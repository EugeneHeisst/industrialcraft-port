package ic2.core.crop;

import ic2.api.crops.ICropTile;
import ic2.core.ref.Ic2Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ItemStack;

public class CropTerraWart extends Ic2CropCard {
   @Override
   public String name() {
      return "terraWart";
   }

   @Override
   public int tier() {
      return 5;
   }

   @Override
   public int stat(int n) {
      switch (n) {
         case 0:
            return 2;
         case 1:
            return 4;
         case 2:
            return 0;
         case 3:
            return 3;
         case 4:
            return 0;
         default:
            return 0;
      }
   }

   @Override
   public String[] attributes() {
      return new String[]{"Blue", "Aether", "Consumable", "Snow"};
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
   public float dropGainChance() {
      return 0.8F;
   }

   @Override
   public int getOptimalHavestSize(ICropTile crop) {
      return 3;
   }

   @Override
   public ItemStack getGain(ICropTile crop) {
      return new ItemStack(Ic2Items.get("terra_wart"), 1);
   }

   @Override
   public void tick(ICropTile crop) {
      TileEntityCrop te = (TileEntityCrop)crop;
      if (crop.isBlockBelow(Blocks.SNOW_BLOCK)) {
         if (this.canGrow(te)) {
            te.growthPoints = (int)((double)te.growthPoints + (double)te.calcGrowthRate() * 0.5);
         }
      } else if (crop.isBlockBelow(Blocks.SOUL_SAND) && crop.getWorld().random.nextInt(300) == 0) {
         crop.setCrop(IC2Crops.cropNetherWart);
      }
   }

   @Override
   public int getrootslength(ICropTile crop) {
      return 5;
   }
}
