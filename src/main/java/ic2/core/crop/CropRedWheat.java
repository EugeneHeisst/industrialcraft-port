package ic2.core.crop;

import ic2.api.crops.ICropTile;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;

public class CropRedWheat extends Ic2CropCard {
   @Override
   public String name() {
      return "redwheat";
   }

   @Override
   public String discoveredBy() {
      return "raa1337";
   }

   @Override
   public int tier() {
      return 6;
   }

   @Override
   public int stat(int n) {
      switch (n) {
         case 0:
            return 3;
         case 1:
            return 0;
         case 2:
            return 0;
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
      return new String[]{"Red", "Redstone", "Wheat"};
   }

   @Override
   public int maxSize() {
      return 7;
   }

   @Override
   public boolean canGrow(ICropTile crop) {
      return crop.getSize() < 7 && crop.getLightLevel() <= 10 && crop.getLightLevel() >= 5;
   }

   @Override
   public boolean canBeHarvested(ICropTile crop) {
      return crop.getSize() == 7;
   }

   @Override
   public float dropGainChance() {
      return 0.5F;
   }

   @Override
   public int getOptimalHavestSize(ICropTile crop) {
      return 7;
   }

   @Override
   public ItemStack getGain(ICropTile crop) {
      BlockPos coords = crop.getLocation();
      return !crop.getWorld().hasNeighborSignal(coords) && !crop.getWorld().random.nextBoolean()
         ? new ItemStack(Items.WHEAT, 1)
         : new ItemStack(Items.REDSTONE, 1);
   }

   @Override
   public int emitRedstone(ICropTile crop) {
      return crop.getSize() == 7 ? 15 : 0;
   }

   @Override
   public int getEmittedLight(ICropTile crop) {
      return crop.getSize() == 7 ? 7 : 0;
   }

   @Override
   public int growthDuration(ICropTile crop) {
      return 600;
   }

   @Override
   public byte getSizeAfterHarvest(ICropTile crop) {
      return 2;
   }
}
