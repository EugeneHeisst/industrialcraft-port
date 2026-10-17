package ic2.core.audit;

import ic2.api.item.ElectricItem;
import ic2.core.ref.Ic2Items;
import ic2.core.util.Keyboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

/**
 * Original ItemElectricTool.onItemRightClick / onItemUse: ElectricItem.manager.use(stack, 0, player) — a right click tops the tool
 * up from charging armour (batpack) without spending EU. Subclasses reach it through super, the wind meter does not.
 */
public final class RightClickChargeAudit {
    private RightClickChargeAudit() {}

    private static double charge(ItemStack s) { return ElectricItem.manager.getCharge(s); }

    private static ItemStack full(Item item) { var s = new ItemStack(item); ElectricItem.manager.charge(s, Double.MAX_VALUE, Integer.MAX_VALUE, true, false); return s; }

    /** Right click in the air with the tool, wearing only the given chest piece; returns {tool EU, pack EU} after the click. */
    private static double[] click(ServerLevel l, Item tool, ItemStack pack, boolean modeSwitch) {
        var p = FakePlayerFactory.getMinecraft(l);
        p.getInventory().clearContent();
        for (var slot : EquipmentSlot.values()) p.setItemSlot(slot, ItemStack.EMPTY);
        p.setItemSlot(EquipmentSlot.CHEST, pack);
        ItemStack held = new ItemStack(tool);
        p.setItemInHand(InteractionHand.MAIN_HAND, held);
        Keyboard.processKeyUpdate(p, modeSwitch ? 1 << Keyboard.Key.modeSwitch.ordinal() : 0);
        try {
            held.use(l, p, InteractionHand.MAIN_HAND);
            return new double[]{charge(p.getMainHandItem()), charge(p.getItemBySlot(EquipmentSlot.CHEST))};
        } finally {
            Keyboard.processKeyUpdate(p, 0);
            p.getInventory().clearContent();
            for (var slot : EquipmentSlot.values()) p.setItemSlot(slot, ItemStack.EMPTY);
        }
    }

    public static void run(ServerLevel l, CropAudit.Check check) {
        double packFull = charge(full(Ic2Items.BATPACK.get()));
        for (var tool : new Item[]{Ic2Items.MINING_DRILL.get(), Ic2Items.CHAINSAW.get(), Ic2Items.ELECTRIC_HOE.get(), Ic2Items.ELECTRIC_TREETAP.get()}) {
            var r = click(l, tool, full(Ic2Items.BATPACK.get()), false);
            check.test("right click tops an empty " + tool + " up from a worn batpack, EU conserved", r[0] > 0 && Math.abs(r[0] + r[1] - packFull) < 1e-6, r[0] + " / " + r[1]);
        }
        var drill = click(l, Ic2Items.MINING_DRILL.get(), full(Ic2Items.BATPACK.get()), false);
        // original chargeFromArmor discharges the pack ignoring transfer limits and fills the tool up to its maximum: drill 30000 of 60000
        check.test("mining drill right click: fills to its 30000 EU maximum from a 60000 EU batpack, as the original", drill[0] == 30000 && drill[1] == 30000, drill[0] + " / " + drill[1]);
        var hoe = click(l, Ic2Items.ELECTRIC_HOE.get(), full(Ic2Items.BATPACK.get()), false);
        check.test("electric hoe right click: fills to its 10000 EU maximum", hoe[0] == 10000 && hoe[1] == 50000, hoe[0] + " / " + hoe[1]);
        var saw = click(l, Ic2Items.CHAINSAW.get(), full(Ic2Items.BATPACK.get()), true);
        check.test("chainsaw shear-mode toggle still reaches the base right click charge", saw[0] > 0, saw[0]);
        var empty = click(l, Ic2Items.MINING_DRILL.get(), new ItemStack(Ic2Items.BATPACK.get()), false);
        check.test("an empty batpack gives nothing and nothing appears from air", empty[0] == 0 && empty[1] == 0, empty[0] + " / " + empty[1]);
        var none = click(l, Ic2Items.MINING_DRILL.get(), ItemStack.EMPTY, false);
        check.test("no charging armour: the click charges nothing", none[0] == 0, none[0]);
        var meter = click(l, Ic2Items.WIND_METER.get(), full(Ic2Items.BATPACK.get()), false);
        check.test("wind meter right click does not draw from the batpack (original overrides without super)", meter[1] == packFull, meter[0] + " / " + meter[1]);
        // a full tool takes nothing
        var p = FakePlayerFactory.getMinecraft(l);
        p.getInventory().clearContent();
        p.setItemSlot(EquipmentSlot.CHEST, full(Ic2Items.BATPACK.get()));
        var fullDrill = full(Ic2Items.MINING_DRILL.get());
        double before = charge(fullDrill);
        p.setItemInHand(InteractionHand.MAIN_HAND, fullDrill);
        fullDrill.use(l, p, InteractionHand.MAIN_HAND);
        check.test("a full tool takes nothing from the pack", charge(p.getMainHandItem()) == before && charge(p.getItemBySlot(EquipmentSlot.CHEST)) == packFull, charge(p.getItemBySlot(EquipmentSlot.CHEST)));
        p.getInventory().clearContent();
        for (var slot : EquipmentSlot.values()) p.setItemSlot(slot, ItemStack.EMPTY);
    }
}
