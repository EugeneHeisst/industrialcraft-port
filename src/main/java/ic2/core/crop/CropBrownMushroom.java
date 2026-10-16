package ic2.core.crop;

import ic2.api.crops.ICropTile;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ItemStack;

public class CropBrownMushroom extends Ic2CropCard {
   @Override
   public String name() {
      return "brownMushroom";
   }

   @Override
   public int tier() {
      return 2;
   }

   @Override
   public int stat(int n) {
      switch (n) {
         case 0:
            return 0;
         case 1:
            return 4;
         case 2:
            return 0;
         case 3:
            return 0;
         case 4:
            return 4;
         default:
            return 0;
      }
   }

   @Override
   public String[] attributes() {
      return new String[]{"Brown", "Food", "Mushroom"};
   }

   @Override
   public int maxSize() {
      return 3;
   }

   @Override
   public int growthDuration(ICropTile crop) {
      return 200;
   }

   @Override
   public boolean canGrow(ICropTile crop) {
      return crop.getSize() < this.maxSize() && crop.getHydrationStorage() > 0;
   }

   @Override
   public int getOptimalHavestSize(ICropTile crop) {
      return 3;
   }

   @Override
   public boolean canBeHarvested(ICropTile crop) {
      return crop.getSize() >= 3;
   }

   @Override
   public ItemStack getGain(ICropTile crop) {
      return crop.getSize() == 3 ? new ItemStack(Blocks.BROWN_MUSHROOM) : null;
   }
}
