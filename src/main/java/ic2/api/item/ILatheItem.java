package ic2.api.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

public interface ILatheItem {
   int getWidth(ItemStack var1);

   int[] getCurrentState(ItemStack var1);

   void setState(ItemStack var1, int var2, int var3);

   ItemStack getOutputItem(ItemStack var1, int var2);

   float getOutputChance(ItemStack var1, int var2);

   ResourceLocation getTexture(ItemStack var1);

   int getHardness(ItemStack var1);

   public interface ILatheTool extends ICustomDamageItem {
      int getHardness(ItemStack var1);
   }
}
