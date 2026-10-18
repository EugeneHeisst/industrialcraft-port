package ic2.core.audit;

import ic2.core.IC2;
import ic2.core.client.ControlTooltips;
import ic2.core.client.KeyboardClient;
import ic2.core.item.tool.ItemNanoSaber;
import ic2.core.ref.Ic2Items;
import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/** Loaded baked models, effect atlas, actual client bindings/tooltips, and reequip behavior. */
public final class IssueClientAudit {
    private static int checks, failed;
    private static void check(String name, boolean ok, Object detail) {
        checks++; if (!ok) failed++;
        IC2.LOG.info("[issue-client-audit] {} {}: {}", ok ? "PASS" : "FAIL", name, detail);
    }
    public static void run(Minecraft mc) {
        var pipe = mc.getItemRenderer().getModel(new ItemStack(Ic2Items.get("mining_pipe")), mc.level, mc.player, 0);
        check("mining pipe has a 3D model", pipe.isGui3d(), pipe);
        var state = ic2.core.ref.Ic2Blocks.CABLES.get(ic2.core.block.wiring.CableType.glass_fibre).get().defaultBlockState();
        var cable = mc.getBlockRenderer().getBlockModel(state);
        var layers = cable.getRenderTypes(state, net.minecraft.util.RandomSource.create(0), net.neoforged.neoforge.client.model.data.ModelData.EMPTY);
        check("glass fibre uses cutout for its original transparent holes", layers.contains(RenderType.cutout()), layers);
        var effect = mc.getMobEffectTextures().get(ic2.core.ref.Ic2Effects.RADIATION);
        check("radiation sprite is stitched", !effect.contents().name().getPath().equals("missingno"), effect.contents().name());
        for (String id : new String[]{"nano_helmet", "quantum_chestplate", "electric_jetpack", "nightvision_goggles", "mining_laser"}) {
            var lines = new ArrayList<Component>();
            var stack = new ItemStack(Ic2Items.get(id));
            var key = KeyboardClient.modeSwitchKey.getKey();
            try {
                KeyboardClient.modeSwitchKey.setKey(com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM.getOrCreate(80));
                ControlTooltips.tooltip(new ItemTooltipEvent(stack, mc.player, lines, TooltipFlag.NORMAL, net.minecraft.world.item.Item.TooltipContext.of(mc.level)));
                String text = lines.stream().map(Component::getString).reduce("", (a, b) -> a + " | " + b);
                check(id + " tooltip uses rebound mode key", text.contains(KeyboardClient.modeSwitchKey.getTranslatedKeyMessage().getString()), text);
            } finally { KeyboardClient.modeSwitchKey.setKey(key); }
        }
        var saber = (ItemNanoSaber)Ic2Items.get("nano_saber");
        var before = saber.getItemStack(150000);
        var after = before.copy();
        ic2.api.item.ElectricItem.manager.discharge(after, 64, Integer.MAX_VALUE, true, false, false);
        check("saber EU changes do not reequip", !saber.shouldCauseReequipAnimation(before, after, false), "64 EU idle drain");
        check("saber slot changes reequip", saber.shouldCauseReequipAnimation(before, after, true), "slot change");
        net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA, after, tag -> tag.putBoolean("active", true));
        check("saber blade toggles still reequip", saber.shouldCauseReequipAnimation(before, after, false), "active toggle");
        for (String key : new String[]{"block.ic2.stirling_kinetic_generator", "item.ic2.pulling_upgrade", "ic2.tooltip.upgrade.pulling"}) {
            String value = net.minecraft.locale.Language.getInstance().getOrDefault(key);
            check("Russian translation " + key, value.matches(".*[А-Яа-яЁё].*"), value);
        }
        var slots = new net.minecraft.world.entity.EquipmentSlot[]{net.minecraft.world.entity.EquipmentSlot.HEAD, net.minecraft.world.entity.EquipmentSlot.CHEST, net.minecraft.world.entity.EquipmentSlot.LEGS, net.minecraft.world.entity.EquipmentSlot.FEET};
        var saved = new ItemStack[4];
        for (int i = 0; i < 4; i++) saved[i] = mc.player.getItemBySlot(slots[i]);
        try {
            var display = java.util.Arrays.stream(net.minecraft.client.gui.Gui.class.getDeclaredMethods())
                .filter(method -> method.getName().contains("ic2$armorDisplay")).findFirst().orElseThrow();
            display.setAccessible(true);
            for (String set : new String[]{"nano", "quantum"}) {
                String[] pieces = {"helmet", "chestplate", "leggings", "boots"};
                for (int i = 0; i < 4; i++) {
                    var armor = (ic2.core.item.armor.ItemArmorElectric)Ic2Items.get(set + "_" + pieces[i]);
                    mc.player.setItemSlot(slots[i], armor.charged());
                }
                check(set + " original charged HUD points", (int)display.invoke(null, mc.player) == (set.equals("nano") ? 18 : 21), display.invoke(null, mc.player));
                check(set + " HUD does not add vanilla armor protection", mc.player.getArmorValue() == 0, mc.player.getArmorValue());
                for (int i = 0; i < 4; i++) ic2.core.item.ElectricItemManager.setRawCharge(mc.player.getItemBySlot(slots[i]), 0);
                check(set + " empty HUD drops to zero", (int)display.invoke(null, mc.player) == 0, display.invoke(null, mc.player));
            }
        } catch (ReflectiveOperationException | RuntimeException error) {
            check("native armor HUD bridge", false, error);
        } finally {
            for (int i = 0; i < 4; i++) mc.player.setItemSlot(slots[i], saved[i]);
        }
        IC2.LOG.info("[issue-client-audit] DONE {} checks, {} failed", checks, failed);
    }
    private IssueClientAudit() {}
}
