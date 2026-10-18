package ic2.core.client;

import ic2.core.IC2;
import ic2.core.item.armor.*;
import ic2.core.item.tool.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/** Resolve the user's current bindings each time, including rebound vanilla jump/use controls. */
@EventBusSubscriber(modid = IC2.MODID, value = Dist.CLIENT)
public final class ControlTooltips {
    private static void add(ItemTooltipEvent event, String action, KeyMapping... keys) {
        var chord = Component.empty();
        for (int i = 0; i < keys.length; i++) {
            if (i > 0) chord.append(" + ");
            chord.append(keys[i].getTranslatedKeyMessage());
        }
        event.getToolTip().add(Component.translatable("ic2.controls." + action, chord).withStyle(ChatFormatting.GRAY));
    }
    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent event) {
        var item = event.getItemStack().getItem();
        var options = Minecraft.getInstance().options;
        if (item instanceof ItemArmorNanoSuit a && a.getType() == ArmorItem.Type.HELMET
            || item instanceof ItemArmorQuantumSuit q && q.getType() == ArmorItem.Type.HELMET
            || item instanceof ItemArmorUtility.NightvisionGoggles) {
            add(event, "nightvision", KeyboardClient.altKey, KeyboardClient.modeSwitchKey);
        }
        if (item instanceof ItemArmorNanoSuit a && a.getType() == ArmorItem.Type.HELMET
            || item instanceof ItemArmorQuantumSuit q && q.getType() == ArmorItem.Type.HELMET)
            add(event, "hud", KeyboardClient.altKey, KeyboardClient.expandinfo);
        if (item instanceof ItemArmorJetpack || item instanceof ItemArmorJetpackElectric
            || item instanceof ItemArmorQuantumSuit q && q.getType() == ArmorItem.Type.CHESTPLATE)
            add(event, "hover", options.keyJump, KeyboardClient.modeSwitchKey);
        if (item instanceof ItemArmorQuantumSuit q && q.getType() == ArmorItem.Type.CHESTPLATE)
            add(event, "jetpack", KeyboardClient.boostKey, KeyboardClient.modeSwitchKey);
        if (item instanceof ItemArmorQuantumSuit q && q.getType() == ArmorItem.Type.BOOTS)
            add(event, "superjump", KeyboardClient.boostKey, options.keyJump);
        if (item instanceof ItemToolMiningLaser || item instanceof ItemToolWrenchElectric || item instanceof ItemElectricToolChainsaw
            || item instanceof ItemSprayer || item instanceof ItemToolPainter
            || item instanceof ItemDrill && event.getItemStack().is(ic2.core.ref.Ic2Items.IRIDIUM_DRILL.get()))
            add(event, "mode", KeyboardClient.modeSwitchKey, options.keyUse);
    }
    private ControlTooltips() {}
}
