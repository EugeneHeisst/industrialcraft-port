package ic2.core.crop;

import ic2.api.crops.ICropTile;
import ic2.core.IC2;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;

public class CropMelon extends Ic2CropCard {
   @Override
   public String name() {
      return "melon";
   }

   @Override
   public String discoveredBy() {
      return "Chao";
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
            return 2;
         case 4:
            return 0;
         default:
            return 0;
      }
   }

   @Override
   public String[] attributes() {
      return new String[]{"Green", "Food", "Stem"};
   }

   @Override
   public int maxSize() {
      return 4;
   }

   @Override
   public boolean canGrow(ICropTile crop) {
      return crop.getSize() <= 3;
   }

   @Override
   public int weightInfluences(ICropTile crop, float humidity, float nutrients, float air) {
      return (int)((double)humidity * 1.1 + (double)nutrients * 0.9 + (double)air);
   }

   @Override
   public boolean canBeHarvested(ICropTile crop) {
      return crop.getSize() == 4;
   }

   @Override
   public int getOptimalHavestSize(ICropTile crop) {
      return 4;
   }

   @Override
   public ItemStack getGain(ICropTile crop) {
      return java.util.concurrent.ThreadLocalRandom.current().nextInt(3) == 0 ? new ItemStack(Blocks.MELON) : new ItemStack(Items.MELON_SLICE, java.util.concurrent.ThreadLocalRandom.current().nextInt(4) + 2);
   }

   @Override
   public ItemStack getSeeds(ICropTile crop) {
      return crop.getGain() <= 1 && crop.getGrowth() <= 1 && crop.getResistance() <= 1
         ? new ItemStack(Items.MELON_SEEDS, java.util.concurrent.ThreadLocalRandom.current().nextInt(2) + 1)
         : super.getSeeds(crop);
   }

   @Override
   public int growthDuration(ICropTile crop) {
      return crop.getSize() == 3 ? 700 : 250;
   }

   @Override
   public byte getSizeAfterHarvest(ICropTile crop) {
      return 3;
   }
}
