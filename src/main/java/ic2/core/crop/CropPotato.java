package ic2.core.crop;

import ic2.api.crops.ICropTile;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;

public class CropPotato extends Ic2CropCard {
   @Override
   public String name() {
      return "potato";
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
      return new String[]{"Yellow", "Food", "Potato"};
   }

   @Override
   public int maxSize() {
      return 4;
   }

   @Override
   public boolean canGrow(ICropTile crop) {
      return crop.getSize() < 4 && crop.getLightLevel() >= 9;
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
      if (crop.getSize() >= 4) {
         return new ItemStack(Items.POISONOUS_POTATO);
      } else {
         return crop.getSize() == 3 ? new ItemStack(Items.POTATO) : null;
      }
   }

   @Override
   public byte getSizeAfterHarvest(ICropTile crop) {
      return 1;
   }
}
