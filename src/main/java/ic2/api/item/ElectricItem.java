package ic2.api.item;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;

public final class ElectricItem {
    /** Gateway manager: dispatches to the item's own manager. Use this one. */
    public static IElectricItemManager manager;
    /** Default implementation for plain {@link IElectricItem}s. */
    public static IElectricItemManager rawManager;
    private static final List<IBackupElectricItemManager> backupManagers = new ArrayList<>();
    private ElectricItem() {}
    public static void registerBackupManager(IBackupElectricItemManager m) { backupManagers.add(m); }
    public static IBackupElectricItemManager getBackupManager(ItemStack stack) {
        for (var m : backupManagers) if (m.handles(stack)) return m;
        return null;
    }
}
