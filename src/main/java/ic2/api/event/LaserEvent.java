package ic2.api.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/** Mining laser events on NeoForge.EVENT_BUS; handlers may change the laser parameters or cancel. */
public abstract class LaserEvent extends Event implements ICancellableEvent {
    public final Level level;
    public final Entity lasershot;
    public LivingEntity owner;
    public float range, power;
    public int blockBreaks;
    public boolean explosive, smelt;

    protected LaserEvent(Level level, Entity lasershot, LivingEntity owner, float range, float power, int blockBreaks, boolean explosive, boolean smelt) {
        this.level = level; this.lasershot = lasershot; this.owner = owner; this.range = range; this.power = power; this.blockBreaks = blockBreaks; this.explosive = explosive; this.smelt = smelt;
    }

    public static class LaserShootEvent extends LaserEvent {
        public final ItemStack laserItem;
        public LaserShootEvent(Level l, Entity shot, LivingEntity owner, float range, float power, int breaks, boolean explosive, boolean smelt, ItemStack laserItem) { super(l, shot, owner, range, power, breaks, explosive, smelt); this.laserItem = laserItem; }
    }

    public static class LaserExplodesEvent extends LaserEvent {
        public float explosionPower, explosionDropRate, explosionEntityDamage;
        public LaserExplodesEvent(Level l, Entity shot, LivingEntity owner, float range, float power, int breaks, boolean explosive, boolean smelt, float explosionPower, float dropRate, float entityDamage) {
            super(l, shot, owner, range, power, breaks, explosive, smelt); this.explosionPower = explosionPower; this.explosionDropRate = dropRate; this.explosionEntityDamage = entityDamage;
        }
    }

    public static class LaserHitsBlockEvent extends LaserEvent {
        public BlockPos pos;
        public Direction side;
        public boolean removeBlock, dropBlock;
        public float dropChance;
        public LaserHitsBlockEvent(Level l, Entity shot, LivingEntity owner, float range, float power, int breaks, boolean explosive, boolean smelt, BlockPos pos, Direction side, float dropChance, boolean removeBlock, boolean dropBlock) {
            super(l, shot, owner, range, power, breaks, explosive, smelt); this.pos = pos; this.side = side; this.dropChance = dropChance; this.removeBlock = removeBlock; this.dropBlock = dropBlock;
        }
    }

    public static class LaserHitsEntityEvent extends LaserEvent {
        public Entity hitEntity;
        public LaserHitsEntityEvent(Level l, Entity shot, LivingEntity owner, float range, float power, int breaks, boolean explosive, boolean smelt, Entity hit) { super(l, shot, owner, range, power, breaks, explosive, smelt); this.hitEntity = hit; }
    }
}
