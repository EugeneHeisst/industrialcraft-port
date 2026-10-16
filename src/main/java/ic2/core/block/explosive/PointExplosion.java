package ic2.core.block.explosive;

import ic2.api.event.ExplosionEvent;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;

/** Dynamite blast: breaks the 3x3x3 blocks around the point whose blast resistance is below power * 10 and hurts entities within 2 blocks. */
public final class PointExplosion {
    private PointExplosion() {}

    public static void explode(Level level, Entity source, LivingEntity owner, double x, double y, double z, float power, float dropRate, int entityDamage) {
        if (level.isClientSide) return;
        if (NeoForge.EVENT_BUS.post(new ExplosionEvent(level, source, x, y, z, power, owner, 0, 1)).isCanceled()) return;
        Explosion vanilla = new Explosion(level, source, x, y, z, power, false, Explosion.BlockInteraction.DESTROY);
        List<BlockPos> affected = new ArrayList<>();
        int bx = Mth.floor(x), by = Mth.floor(y), bz = Mth.floor(z);
        for (BlockPos p : BlockPos.betweenClosed(bx - 1, by - 1, bz - 1, bx + 1, by + 1, bz + 1)) {
            BlockState s = level.getBlockState(p);
            if (!s.isAir() && s.getExplosionResistance(level, p, vanilla) < power * 10) affected.add(p.immutable());
        }
        for (Entity e : level.getEntities(source, new AABB(x - 2, y - 2, z - 2, x + 2, y + 2, z + 2)))
            e.hurt(level.damageSources().explosion(source, owner), entityDamage);
        level.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 4, (1 + (level.random.nextFloat() - level.random.nextFloat()) * 0.2f) * 0.7f);
        if (level instanceof ServerLevel sl) sl.sendParticles(ParticleTypes.EXPLOSION, x, y, z, 1, 0, 0, 0, 0);
        float chance = dropRate; // the original sets explosionSize = 1 / dropRate before vanilla drops (drop chance 1 / size)
        for (BlockPos p : affected) {
            BlockState s = level.getBlockState(p);
            if (s.isAir()) continue;
            if (level.random.nextFloat() <= chance) Block.dropResources(s, level, p, level.getBlockEntity(p), source, net.minecraft.world.item.ItemStack.EMPTY);
            s.onBlockExploded(level, p, vanilla);
        }
    }
}
