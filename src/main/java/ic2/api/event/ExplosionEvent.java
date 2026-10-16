package ic2.api.event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/** Posted before an IC2 explosion (nuke, reactor, ITNT, mining laser); cancel to prevent it. */
public class ExplosionEvent extends Event implements ICancellableEvent {
    public final Level level;
    public final Entity entity;
    public double x, y, z, power;
    public final LivingEntity igniter;
    public final int radiationRange;
    public final double rangeLimit;

    public ExplosionEvent(Level level, Entity entity, double x, double y, double z, double power, LivingEntity igniter, int radiationRange, double rangeLimit) {
        this.level = level; this.entity = entity; this.x = x; this.y = y; this.z = z; this.power = power; this.igniter = igniter; this.radiationRange = radiationRange; this.rangeLimit = rangeLimit;
    }
}
