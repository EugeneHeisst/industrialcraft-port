package ic2.core.crop;

import ic2.api.crops.ICropTile;
import ic2.core.ref.Ic2Items;
import net.minecraft.world.item.ItemStack;

public class CropPlumbiscus extends Ic2CropCard {
   public ItemStack mDrop;

   @Override
   public String name() {
      return "plumbiscus";
   }

   @Override
   public int tier() {
      return 6;
   }

   @Override
   public int stat(int n) {
      switch (n) {
         case 0:
            return 2;
         case 1:
            return 0;
         case 2:
            return 0;
         case 3:
            return 1;
         case 4:
            return 0;
         default:
            return 0;
      }
   }

   @Override
   public String[] attributes() {
      return new String[]{"Dense", "Leaves", "Metal"};
   }

   @Override
   public int maxSize() {
      return 4;
   }

   @Override
   public boolean canGrow(ICropTile crop) {
      return crop.getSize() < 3 ? true : crop.getSize() == 3 && (crop.isBlockBelow("oreLead") || crop.isBlockBelow("blockLead"));
   }

   @Override
   public int getrootslength(ICropTile crop) {
      return 5;
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
      if (this.mDrop == null) {
         this.mDrop = new ItemStack(Ic2Items.get("small_lead_dust"));
      }

      return this.mDrop.copy();
   }

   @Override
   public float dropGainChance() {
      return super.dropGainChance() / 2.0F;
   }

   @Override
   public int growthDuration(ICropTile crop) {
      return crop.getSize() == 3 ? 2000 : 800;
   }

   @Override
   public byte getSizeAfterHarvest(ICropTile crop) {
      return 2;
   }
}
