package ic2.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/** Single tick callbacks of the original, run at the start of the next tick of their level. */
public final class TickHandler {
    private static final Map<Level, List<Consumer<Level>>> singleTickCallbacks = new WeakHashMap<>();

    public static synchronized void addSingleTickCallback(Level level, Consumer<Level> callback) {
        singleTickCallbacks.computeIfAbsent(level, l -> new ArrayList<>()).add(callback);
    }

    @SubscribeEvent
    public void onLevelTick(LevelTickEvent.Pre event) {
        List<Consumer<Level>> callbacks;
        synchronized (TickHandler.class) {
            callbacks = singleTickCallbacks.remove(event.getLevel());
        }
        if (callbacks != null) for (var callback : callbacks) callback.accept(event.getLevel());
    }
}
