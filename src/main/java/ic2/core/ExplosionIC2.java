package ic2.core;

import ic2.api.event.ExplosionEvent;
import ic2.api.tile.ExplosionWhitelist;
import ic2.core.item.armor.ItemArmorUtility;
import ic2.core.ref.Ic2Effects;
import ic2.core.util.Ic2DamageSources;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;

/**
 * IC2's ray-traced explosion: rays lose power by blast resistance, blocks hit by strong rays (> 8) drop nothing, drops are merged in
 * 2x2 columns. Types: normal, heat (resistance counts 6x), nuclear (nuke damage, hunger and radiation around).
 */
public class ExplosionIC2 {
    public enum Type { Normal, Heat, Nuclear }

    private final Random rng = new Random();
    private final Level level;
    private final Entity exploder;
    private final double x, y, z;
    private final float power, dropRate;
    private final Type type;
    private final int radiationRange;
    private final LivingEntity igniter;
    private final double maxDistance;
    private final DamageSource damageSource;
    private final List<EntityDamage> entitiesInRange = new ArrayList<>();
    /** destroyed positions -> true when the block drops nothing */
    private final Map<BlockPos, Boolean> destroyed = new LinkedHashMap<>();
    private final Explosion vanilla;

    public ExplosionIC2(Level level, Entity exploder, double x, double y, double z, float power, float dropRate, Type type, LivingEntity igniter, int radiationRange) {
        this.level = level; this.exploder = exploder; this.x = x; this.y = y; this.z = z; this.power = power; this.dropRate = dropRate;
        this.type = type; this.igniter = igniter; this.radiationRange = radiationRange;
        maxDistance = power / 0.4;
        damageSource = type == Type.Nuclear ? Ic2DamageSources.get(level, Ic2DamageSources.NUKE) : level.damageSources().explosion(exploder, igniter);
        vanilla = new Explosion(level, exploder, x, y, z, power, false, Explosion.BlockInteraction.DESTROY);
    }

    public ExplosionIC2(Level level, Entity exploder, double x, double y, double z, float power, float dropRate) { this(level, exploder, x, y, z, power, dropRate, Type.Normal, null, 0); }

    public static void explode(Level level, Entity exploder, LivingEntity igniter, double x, double y, double z, float power, float dropRate) {
        new ExplosionIC2(level, exploder, x, y, z, power, dropRate, Type.Normal, igniter, 0).doExplosion();
    }

    public void doExplosion() {
        if (power <= 0 || level.isClientSide) return;
        if (NeoForge.EVENT_BUS.post(new ExplosionEvent(level, exploder, x, y, z, power, igniter, radiationRange, maxDistance)).isCanceled()) return;
        for (Entity e : level.getEntities((Entity)null, new AABB(x - maxDistance, y - maxDistance, z - maxDistance, x + maxDistance, y + maxDistance, z + maxDistance))) {
            if (e instanceof LivingEntity || e instanceof ItemEntity)
                entitiesInRange.add(new EntityDamage(e, (int)(Mth.square(e.getX() - x) + Mth.square(e.getY() - y) + Mth.square(e.getZ() - z)), e instanceof ItemEntity ? 5 : Double.POSITIVE_INFINITY));
        }
        boolean entities = !entitiesInRange.isEmpty();
        entitiesInRange.sort((a, b) -> a.distance - b.distance);
        int steps = (int)Math.ceil(Math.PI / Math.atan(1 / maxDistance));
        for (int phi = 0; phi < 2 * steps; phi++)
            for (int theta = 0; theta < steps; theta++)
                shootRay(x, y, z, 2 * Math.PI / steps * phi, Math.PI / steps * theta, power, entities && phi % 8 == 0 && theta % 8 == 0);
        for (EntityDamage entry : entitiesInRange) {
            Entity e = entry.entity;
            e.hurt(damageSource, (float)entry.damage);
            double motionSq = Mth.square(entry.mx) + Mth.square(e.getDeltaMovement().y) + Mth.square(e.getDeltaMovement().z);
            double reduction = motionSq > 3600 ? Math.sqrt(3600 / motionSq) : 1;
            e.setDeltaMovement(e.getDeltaMovement().add(entry.mx * reduction, entry.my * reduction, entry.mz * reduction));
            e.hurtMarked = true;
        }
        if (type == Type.Nuclear && radiationRange >= 1) {
            for (Mob m : level.getEntitiesOfClass(Mob.class, new AABB(x, y, z, x, y, z).inflate(radiationRange))) {
                if (ItemArmorUtility.Hazmat.hasCompleteHazmat(m)) continue;
                double d = m.position().distanceTo(new Vec3(x, y, z));
                int hunger = (int)(120 * (radiationRange - d)), poison = (int)(80 * (radiationRange / 3 - d));
                if (hunger >= 0) m.addEffect(new MobEffectInstance(MobEffects.HUNGER, hunger, 0));
                if (poison >= 0) m.addEffect(new MobEffectInstance(Ic2Effects.RADIATION, poison, 0));
            }
        }
        level.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 4, (1 + (level.random.nextFloat() - level.random.nextFloat()) * 0.2f) * 0.7f);
        if (level instanceof ServerLevel sl) sl.sendParticles(power >= 4 ? ParticleTypes.EXPLOSION_EMITTER : ParticleTypes.EXPLOSION, x, y, z, 1, 0, 0, 0, 0);
        boolean doDrops = level.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS);
        Map<Long, Map<ItemKey, int[]>> drops = new HashMap<>();
        for (var entry : destroyed.entrySet()) {
            BlockPos p = entry.getKey();
            BlockState state = level.getBlockState(p);
            if (state.isAir()) continue;
            if (power < 20 && level instanceof ServerLevel sl && rng.nextInt(4) == 0) sl.sendParticles(ParticleTypes.SMOKE, p.getX() + rng.nextFloat(), p.getY() + rng.nextFloat(), p.getZ() + rng.nextFloat(), 1, 0, 0, 0, 0.05);
            if (doDrops && !entry.getValue() && level instanceof ServerLevel sl) {
                for (ItemStack stack : Block.getDrops(state, sl, p, level.getBlockEntity(p), exploder, ItemStack.EMPTY)) {
                    if (rng.nextFloat() > dropRate) continue;
                    long key = (long)Math.floorDiv(p.getX(), 2) << 32 | (Math.floorDiv(p.getZ(), 2) & 0xffffffffL);
                    int[] data = drops.computeIfAbsent(key, k -> new LinkedHashMap<>()).computeIfAbsent(new ItemKey(stack), k -> new int[]{0, p.getY()});
                    data[0] += stack.getCount();
                    data[1] = Math.max(data[1], p.getY());
                }
            }
            state.onBlockExploded(level, p, vanilla);
            if (!level.getBlockState(p).isAir()) level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
        }
        for (var col : drops.entrySet()) {
            int cx = (int)(col.getKey() >> 32), cz = (int)(long)col.getKey();
            for (var e : col.getValue().entrySet()) {
                int count = e.getValue()[0];
                while (count > 0) {
                    int n = Math.min(count, 64);
                    var item = new ItemEntity(level, (cx + level.random.nextFloat()) * 2, e.getValue()[1] + 0.5, (cz + level.random.nextFloat()) * 2, e.getKey().stack.copyWithCount(n));
                    item.setPickUpDelay(10);
                    level.addFreshEntity(item);
                    count -= n;
                }
            }
        }
    }

    private void shootRay(double x, double y, double z, double phi, double theta, double power, boolean killEntities) {
        double dx = Math.sin(theta) * Math.cos(phi), dy = Math.cos(theta), dz = Math.sin(theta) * Math.sin(phi);
        for (int step = 0; ; step++) {
            int by = Mth.floor(y);
            if (by < level.getMinBuildHeight() || by >= level.getMaxBuildHeight()) break;
            BlockPos p = new BlockPos(Mth.floor(x), by, Mth.floor(z));
            BlockState state = level.getBlockState(p);
            double absorption = absorption(state, p);
            if (absorption > 1000 && !ExplosionWhitelist.isBlockWhitelisted(state.getBlock())) absorption = 0.5;
            else {
                if (absorption > power) break;
                if (!state.isAir()) destroyed.merge(p, power > 8, (a, b) -> a || b);
            }
            if (killEntities && (step + 4) % 8 == 0 && !entitiesInRange.isEmpty() && power >= 0.25) damageEntities(x, y, z, step, power);
            if (absorption > 10) for (int i = 0; i < 5; i++) shootRay(x, y, z, rng.nextDouble() * 2 * Math.PI, rng.nextDouble() * Math.PI, absorption * 0.4, false);
            power -= absorption;
            x += dx; y += dy; z += dz;
        }
    }

    private double absorption(BlockState state, BlockPos p) {
        if (state.isAir()) return 0.5;
        if (state.is(Blocks.WATER) && type != Type.Normal) return 1.5;
        double extra = (state.getExplosionResistance(level, p, vanilla) + 4) * 0.3;
        return 0.5 + (type == Type.Heat ? extra * 6 : extra);
    }

    private void damageEntities(double px, double py, double pz, int step, double power) {
        int distanceMax = Mth.square(step + 5), distanceMin = step == 4 ? 0 : Mth.square(step - 5);
        for (int i = 0; i < entitiesInRange.size(); i++) {
            EntityDamage entry = entitiesInRange.get(i);
            if (entry.distance < distanceMin) continue;
            if (entry.distance >= distanceMax) break;
            Entity e = entry.entity;
            if (Mth.square(e.getX() - px) + Mth.square(e.getY() - py) + Mth.square(e.getZ() - pz) > 25) continue;
            double damage = 4 * power;
            entry.damage += damage;
            entry.health -= damage;
            double ddx = e.getX() - x, ddy = e.getY() - y, ddz = e.getZ() - z, dist = Math.sqrt(ddx * ddx + ddy * ddy + ddz * ddz);
            if (dist > 0) { entry.mx += ddx / dist * 0.0875 * power; entry.my += ddy / dist * 0.0875 * power; entry.mz += ddz / dist * 0.0875 * power; }
            if (entry.health <= 0) {
                e.hurt(damageSource, (float)entry.damage);
                if (!e.isAlive()) { entitiesInRange.remove(i); i--; }
            }
        }
    }

    public LivingEntity getExplosivePlacedBy() { return igniter; }

    private static final class EntityDamage {
        final Entity entity; final int distance; double health, damage, mx, my, mz;
        EntityDamage(Entity entity, int distance, double health) { this.entity = entity; this.distance = distance; this.health = health; }
    }

    private record ItemKey(ItemStack stack) {
        @Override public boolean equals(Object o) { return o instanceof ItemKey k && ItemStack.isSameItemSameComponents(stack, k.stack); }
        @Override public int hashCode() { return ItemStack.hashItemAndComponents(stack); }
    }
}
