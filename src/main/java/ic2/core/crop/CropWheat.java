package ic2.core.crop;

import ic2.api.crops.ICropTile;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;

public class CropWheat extends Ic2CropCard {
   @Override
   public String name() {
      return "wheat";
   }

   @Override
   public String discoveredBy() {
      return "Notch";
   }

   @Override
   public int tier() {
      return 1;
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
            return 2;
         default:
            return 0;
      }
   }

   @Override
   public String[] attributes() {
      return new String[]{"Yellow", "Food", "Wheat"};
   }

   @Override
   public int maxSize() {
      return 7;
   }

   @Override
   public boolean canGrow(ICropTile crop) {
      return crop.getSize() < 7 && crop.getLightLevel() >= 9;
   }

   @Override
   public int getOptimalHavestSize(ICropTile crop) {
      return 7;
   }

   @Override
   public boolean canBeHarvested(ICropTile crop) {
      return crop.getSize() == 7;
   }

   @Override
   public ItemStack getGain(ICropTile crop) {
      return new ItemStack(Items.WHEAT, 1);
   }

   @Override
   public ItemStack getSeeds(ICropTile crop) {
      return crop.getGain() <= 1 && crop.getGrowth() <= 1 && crop.getResistance() <= 1 ? new ItemStack(Items.WHEAT_SEEDS) : super.getSeeds(crop);
   }

   @Override
   public byte getSizeAfterHarvest(ICropTile crop) {
      return 2;
   }
}
