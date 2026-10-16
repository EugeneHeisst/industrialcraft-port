package ic2.core.crop;

import ic2.api.crops.ICropTile;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;

public class CropNetherWart extends Ic2CropCard {
   @Override
   public String name() {
      return "netherWart";
   }

   @Override
   public String discoveredBy() {
      return "Notch";
   }

   @Override
   public int tier() {
      return 5;
   }

   @Override
   public int stat(int n) {
      switch (n) {
         case 0:
            return 4;
         case 1:
            return 2;
         case 2:
            return 0;
         case 3:
            return 2;
         case 4:
            return 1;
         default:
            return 0;
      }
   }

   @Override
   public String[] attributes() {
      return new String[]{"Red", "Nether", "Ingredient", "Soulsand"};
   }

   @Override
   public int maxSize() {
      return 3;
   }

   @Override
   public boolean canGrow(ICropTile crop) {
      return crop.getSize() < 3;
   }

   @Override
   public boolean canBeHarvested(ICropTile crop) {
      return crop.getSize() == 3;
   }

   @Override
   public int getOptimalHavestSize(ICropTile crop) {
      return 3;
   }

   @Override
   public float dropGainChance() {
      return 2.0F;
   }

   @Override
   public ItemStack getGain(ICropTile crop) {
      return new ItemStack(Items.NETHER_WART, 1);
   }

   @Override
   public void tick(ICropTile crop) {
      TileEntityCrop te = (TileEntityCrop)crop;
      if (te.isBlockBelow(Blocks.SOUL_SAND)) {
         if (this.canGrow(te)) {
            te.growthPoints = (int)((double)te.growthPoints + (double)te.calcGrowthRate() * 0.5);
         }
      } else if (te.isBlockBelow(Blocks.SNOW_BLOCK) && crop.getWorld().random.nextInt(300) == 0) {
         te.setCrop(IC2Crops.cropTerraWart);
      }
   }

   @Override
   public int getrootslength(ICropTile crop) {
      return 5;
   }
}
