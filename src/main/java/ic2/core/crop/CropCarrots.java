package ic2.core.crop;

import ic2.api.crops.ICropTile;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;

public class CropCarrots extends Ic2CropCard {
   @Override
   public String name() {
      return "carrots";
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
            return 2;
         default:
            return 0;
      }
   }

   @Override
   public String[] attributes() {
      return new String[]{"Orange", "Food", "Carrots"};
   }

   @Override
   public int maxSize() {
      return 3;
   }

   @Override
   public boolean canGrow(ICropTile crop) {
      return crop.getSize() < 3 && crop.getLightLevel() >= 9;
   }

   @Override
   public int getOptimalHavestSize(ICropTile crop) {
      return 3;
   }

   @Override
   public boolean canBeHarvested(ICropTile crop) {
      return crop.getSize() == 3;
   }

   @Override
   public ItemStack getGain(ICropTile crop) {
      return new ItemStack(Items.CARROT).copy();
   }

   @Override
   public byte getSizeAfterHarvest(ICropTile crop) {
      return 1;
   }
}
