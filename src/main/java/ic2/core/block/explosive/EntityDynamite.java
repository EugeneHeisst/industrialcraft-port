package ic2.core.block.explosive;

import ic2.core.ref.Ic2Entities;
import ic2.core.ref.Ic2Items;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Thrown or ignited dynamite: 100-tick fuse, bounces (sticky ones stay stuck and burn 4x faster), water adds 2000 ticks. */
public class EntityDynamite extends Projectile implements ItemSupplier {
    public boolean sticky;
    public int fuse = 100;
    private boolean inGround;
    private BlockPos stick = BlockPos.ZERO;
    private int ticksInGround;

    public EntityDynamite(EntityType<? extends EntityDynamite> type, Level level) { super(type, level); }

    public EntityDynamite(Level level, double x, double y, double z, boolean sticky) {
        this(Ic2Entities.DYNAMITE.get(), level);
        setPos(x, y, z);
        this.sticky = sticky;
    }

    /** Thrown from the player's eyes with speed 1 and the original 0.0075 spread. */
    public EntityDynamite(Level level, LivingEntity thrower, boolean sticky) {
        this(Ic2Entities.DYNAMITE.get(), level);
        this.sticky = sticky;
        setOwner(thrower);
        float yaw = thrower.getYRot(), pitch = thrower.getXRot();
        moveTo(thrower.getX() - Mth.cos(yaw / 180 * Mth.PI) * 0.16, thrower.getEyeY() - 0.1, thrower.getZ() - Mth.sin(yaw / 180 * Mth.PI) * 0.16, yaw, pitch);
        shoot(-Mth.sin(yaw / 180 * Mth.PI) * Mth.cos(pitch / 180 * Mth.PI), -Mth.sin(pitch / 180 * Mth.PI), Mth.cos(yaw / 180 * Mth.PI) * Mth.cos(pitch / 180 * Mth.PI), 1, 1);
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder b) {}
    @Override public ItemStack getItem() { return new ItemStack(sticky ? Ic2Items.STICKY_DYNAMITE.get() : Ic2Items.DYNAMITE.get()); }

    @Override
    public void tick() {
        super.tick();
        if (fuse-- <= 0) {
            discard();
            if (!level().isClientSide) PointExplosion.explode(level(), this, getOwner() instanceof LivingEntity l ? l : null, getX(), getY(), getZ(), 1, 1, 20);
            return;
        }
        if (fuse < 100 && fuse % 2 == 0) level().addParticle(ParticleTypes.SMOKE, getX(), getY() + 0.5, getZ(), 0, 0, 0);
        if (inGround) {
            if (++ticksInGround >= 200) discard();
            if (sticky) {
                fuse -= 3;
                setDeltaMovement(Vec3.ZERO);
                if (!level().getBlockState(stick).isAir()) return;
            }
        }
        Vec3 m = getDeltaMovement(), from = position(), to = from.add(m);
        BlockHitResult hit = level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        double nx, ny, nz;
        if (hit.getType() != HitResult.Type.MISS) {
            Vec3 remain = hit.getLocation().subtract(from);
            double len = remain.length();
            stick = hit.getBlockPos();
            Vec3 back = len > 0 ? remain.scale(0.05 / len) : Vec3.ZERO;
            nx = getX() + remain.x - back.x; ny = getY() + remain.y - back.y; nz = getZ() + remain.z - back.z;
            m = new Vec3(m.x * (0.75 - random.nextFloat()), m.y * -0.3, m.z * (0.75 - random.nextFloat()));
            inGround = true;
        } else {
            nx = getX() + m.x; ny = getY() + m.y; nz = getZ() + m.z;
            inGround = false;
        }
        double drag = 0.98;
        if (isInWater()) {
            fuse += 2000;
            for (int i = 0; i < 4; i++) level().addParticle(ParticleTypes.BUBBLE, nx - m.x * 0.25, ny - m.y * 0.25, nz - m.z * 0.25, m.x, m.y, m.z);
            drag = 0.75;
        }
        setDeltaMovement(m.x * drag, m.y * drag - 0.04, m.z * drag);
        setPos(nx, ny, nz);
        updateRotation();
    }

    @Override protected void addAdditionalSaveData(CompoundTag tag) { super.addAdditionalSaveData(tag); tag.putBoolean("inGround", inGround); tag.putBoolean("sticky", sticky); tag.putInt("fuse", fuse); tag.putLong("stick", stick.asLong()); }
    @Override protected void readAdditionalSaveData(CompoundTag tag) { super.readAdditionalSaveData(tag); inGround = tag.getBoolean("inGround"); sticky = tag.getBoolean("sticky"); fuse = tag.contains("fuse") ? tag.getInt("fuse") : 100; stick = BlockPos.of(tag.getLong("stick")); }
}
