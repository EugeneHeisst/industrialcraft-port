package ic2.core.crop;

import ic2.api.crops.ICropTile;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;

public class CropCocoa extends Ic2CropCard {
   @Override
   public String name() {
      return "cocoa";
   }

   @Override
   public String discoveredBy() {
      return "Notch";
   }

   @Override
   public int tier() {
      return 3;
   }

   @Override
   public int stat(int n) {
      switch (n) {
         case 0:
            return 1;
         case 1:
            return 3;
         case 2:
            return 0;
         case 3:
            return 4;
         case 4:
            return 0;
         default:
            return 0;
      }
   }

   @Override
   public String[] attributes() {
      return new String[]{"Brown", "Food", "Stem"};
   }

   @Override
   public int maxSize() {
      return 4;
   }

   @Override
   public boolean canGrow(ICropTile crop) {
      return crop.getSize() <= 3 && crop.getNutrients() >= 3;
   }

   @Override
   public int weightInfluences(ICropTile crop, float humidity, float nutrients, float air) {
      return (int)((double)humidity * 0.8 + (double)nutrients * 1.3 + (double)air * 0.9);
   }

   @Override
   public int getOptimalHavestSize(ICropTile crop) {
      return 4;
   }

   @Override
   public boolean canBeHarvested(ICropTile crop) {
      return crop.getSize() == 4;
   }

   @Override
   public ItemStack getGain(ICropTile crop) {
      return new ItemStack(Items.COCOA_BEANS);
   }

   @Override
   public int growthDuration(ICropTile crop) {
      return crop.getSize() == 3 ? 900 : 400;
   }

   @Override
   public byte getSizeAfterHarvest(ICropTile crop) {
      return 3;
   }
}
