package ic2.core.item.boat;

import ic2.api.item.ElectricItem;
import ic2.core.IC2;
import ic2.core.ref.Ic2Items;
import ic2.core.util.Keyboard;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * IC2 boats on top of the modern boat physics. Like the original they break when they crash faster than their break
 * speed: the carbon boat only above 0.4 (faster than a boat goes on water), the rubber boat pops (and tears at more than
 * 0.26). The electric boat burns 4 EU per tick from the rider's armour to go twice as fast, crawls without it, and also
 * floats on lava.
 */
public class EntityIC2Boat extends Boat {
    public enum Kind {
        CARBON(0.4, "boatcarbon"), RUBBER(0.23, "boatrubber"), ELECTRIC(0.5, "boatelectric");
        public final double breakMotion;
        public final String texture;
        Kind(double breakMotion, String texture) { this.breakMotion = breakMotion; this.texture = texture; }
    }

    private static final EntityDataAccessor<Boolean> ACCELERATED = SynchedEntityData.defineId(EntityIC2Boat.class, EntityDataSerializers.BOOLEAN);
    public static final double EU_CONSUME = 4, TOP_SPEED_ELECTRIC = 0.7;
    public final Kind kind;

    public EntityIC2Boat(EntityType<? extends Boat> type, Level level, Kind kind) { super(type, level); this.kind = kind; }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ACCELERATED, false);
    }

    public boolean isAccelerated() { return entityData.get(ACCELERATED); }

    @Override
    public Item getDropItem() {
        return switch (kind) { case CARBON -> Ic2Items.CARBON_BOAT.get(); case RUBBER -> Ic2Items.RUBBER_BOAT.get(); case ELECTRIC -> Ic2Items.ELECTRIC_BOAT.get(); };
    }

    @Override public ItemStack getPickResult() { return new ItemStack(getDropItem()); }

    @Override
    public void tick() {
        if (kind == Kind.ELECTRIC) {
            clearFire();
            if (!level().isClientSide) entityData.set(ACCELERATED, getControllingPassenger() instanceof Player rider && payForBoost(rider));
        }
        double speed = getDeltaMovement().horizontalDistance();
        boolean local = isControlledByLocalInstance();
        super.tick();
        if (!isAlive()) return;
        if (local && kind == Kind.ELECTRIC && getControllingPassenger() instanceof Player rider && rider.zza > 0) {
            // the boat paddles 0.04 per tick forward: doubled with charge, a quarter without
            Vec3 dir = Vec3.directionFromRotation(0, getYRot());
            Vec3 v = getDeltaMovement().add(dir.scale(isAccelerated() ? 0.04 : -0.03));
            double h = v.horizontalDistance();
            if (h > TOP_SPEED_ELECTRIC) v = new Vec3(v.x * TOP_SPEED_ELECTRIC / h, v.y, v.z * TOP_SPEED_ELECTRIC / h);
            setDeltaMovement(v);
        }
        if (local && horizontalCollision && speed > kind.breakMotion) {
            if (level().isClientSide) PacketDistributor.sendToServer(new CrashPayload(getId(), speed));
            else crash(speed);
        }
    }

    /** Server: pays the rider's EU for this tick's boost, from the first armour piece that has 4 EU. */
    public boolean payForBoost(Player rider) {
        if (!(Keyboard.isForwardKeyDown(rider) || rider.zza > 0)) return false;
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD}) {
            ItemStack stack = rider.getItemBySlot(slot);
            if (!stack.isEmpty() && ElectricItem.manager.discharge(stack, EU_CONSUME, Integer.MAX_VALUE, true, true, true) == EU_CONSUME) {
                ElectricItem.manager.discharge(stack, EU_CONSUME, Integer.MAX_VALUE, true, true, false);
                return true;
            }
        }
        return false;
    }

    public void crash(double speed) {
        if (!isAlive() || level().isClientSide) return;
        ejectPassengers();
        discard();
        if (kind == Kind.RUBBER) {
            playSound(SoundEvents.ITEM_PICKUP, 16, 2);
            spawnAtLocation(new ItemStack(speed > 0.26 ? Ic2Items.BROKEN_RUBBER_BOAT.get() : Ic2Items.RUBBER_BOAT.get()));
        } else spawnAtLocation(new ItemStack(getDropItem()));
    }

    @Override public boolean canBoatInFluid(FluidState state) { return kind == Kind.ELECTRIC && state.is(net.minecraft.tags.FluidTags.LAVA) || super.canBoatInFluid(state); }
    @Override public boolean canBoatInFluid(FluidType type) { return kind == Kind.ELECTRIC && type == net.neoforged.neoforge.common.NeoForgeMod.LAVA_TYPE.value() || super.canBoatInFluid(type); }

    /** Client → server: the boat the sender steers crashed at that speed. */
    public record CrashPayload(int entity, double speed) implements CustomPacketPayload {
        public static final Type<CrashPayload> TYPE = new Type<>(IC2.id("boat_crash"));
        public static final StreamCodec<ByteBuf, CrashPayload> CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, CrashPayload::entity, ByteBufCodecs.DOUBLE, CrashPayload::speed, CrashPayload::new);
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

        public static void handle(CrashPayload p, IPayloadContext ctx) {
            if (ctx.player().level().getEntity(p.entity) instanceof EntityIC2Boat boat && boat.getControllingPassenger() == ctx.player() && p.speed > boat.kind.breakMotion && p.speed < 4) boat.crash(p.speed);
        }
    }
}
