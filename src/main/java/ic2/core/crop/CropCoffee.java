package ic2.core.crop;

import ic2.api.crops.ICropTile;
import ic2.core.ref.Ic2Items;
import net.minecraft.world.item.ItemStack;

public class CropCoffee extends Ic2CropCard {
   @Override
   public String name() {
      return "coffee";
   }

   @Override
   public String discoveredBy() {
      return "Snoochy";
   }

   @Override
   public int tier() {
      return 7;
   }

   @Override
   public int stat(int n) {
      switch (n) {
         case 0:
            return 1;
         case 1:
            return 4;
         case 2:
            return 1;
         case 3:
            return 2;
         case 4:
            return 0;
         default:
            return 0;
      }
   }

   @Override
   public String[] attributes() {
      return new String[]{"Leaves", "Ingredient", "Beans"};
   }

   @Override
   public int maxSize() {
      return 5;
   }

   @Override
   public boolean canGrow(ICropTile crop) {
      return crop.getSize() < 5 && crop.getLightLevel() >= 9;
   }

   @Override
   public int weightInfluences(ICropTile crop, float humidity, float nutrients, float air) {
      return (int)(0.4 * (double)humidity + 1.4 * (double)nutrients + 1.2 * (double)air);
   }

   @Override
   public int growthDuration(ICropTile crop) {
      if (crop.getSize() == 3) {
         return (int)((double)super.growthDuration(crop) * 0.5);
      } else {
         return crop.getSize() == 4 ? (int)((double)super.growthDuration(crop) * 1.5) : super.growthDuration(crop);
      }
   }

   @Override
   public boolean canBeHarvested(ICropTile crop) {
      return crop.getSize() >= 4;
   }

   @Override
   public int getOptimalHavestSize(ICropTile crop) {
      return 5;
   }

   @Override
   public ItemStack getGain(ICropTile crop) {
      return crop.getSize() == 4 ? null : new ItemStack(Ic2Items.get("coffee_beans"));
   }

   @Override
   public byte getSizeAfterHarvest(ICropTile crop) {
      return 3;
   }
}
