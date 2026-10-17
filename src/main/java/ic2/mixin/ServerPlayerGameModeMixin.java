package ic2.mixin;

import ic2.core.item.tool.ItemElectricTool;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The original onBlockStartBreak ran after the break event and before the drops. destroyBlock copies the tool for the loot right
 * after every check has passed (break event included); this lets an IC2 electric tool act at exactly that point.
 */
@Mixin(ServerPlayerGameMode.class)
abstract class ServerPlayerGameModeMixin {
    @Shadow protected ServerLevel level;
    @Shadow @Final protected ServerPlayer player;

    @Inject(method = "destroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;copy()Lnet/minecraft/world/item/ItemStack;"))
    private void ic2$beforeToolSnapshot(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        var stack = player.getMainHandItem();
        if (stack.getItem() instanceof ItemElectricTool tool) tool.beforeHarvestSnapshot(stack, level, pos, level.getBlockState(pos), player);
    }
}
