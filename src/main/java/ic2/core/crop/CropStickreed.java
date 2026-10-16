package ic2.core.crop;

import ic2.api.crops.ICropTile;
import ic2.core.IC2;
import ic2.core.ref.Ic2Items;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;

public class CropStickreed extends Ic2CropCard {
   @Override
   public String name() {
      return "stickreed";
   }

   @Override
   public String discoveredBy() {
      return "raa1337";
   }

   @Override
   public int tier() {
      return 4;
   }

   @Override
   public int stat(int n) {
      switch (n) {
         case 0:
            return 2;
         case 1:
            return 0;
         case 2:
            return 1;
         case 3:
            return 0;
         case 4:
            return 1;
         default:
            return 0;
      }
   }

   @Override
   public String[] attributes() {
      return new String[]{"Reed", "Resin"};
   }

   @Override
   public int maxSize() {
      return 4;
   }

   @Override
   public boolean canGrow(ICropTile crop) {
      return crop.getSize() < 4;
   }

   @Override
   public int weightInfluences(ICropTile crop, float humidity, float nutrients, float air) {
      return (int)((double)humidity * 1.2 + (double)nutrients + (double)air * 0.8);
   }

   @Override
   public boolean canBeHarvested(ICropTile crop) {
      return crop.getSize() > 1;
   }

   @Override
   public int getOptimalHavestSize(ICropTile crop) {
      return 4;
   }

   @Override
   public ItemStack getGain(ICropTile crop) {
      return crop.getSize() <= 3 ? new ItemStack(Items.SUGAR_CANE, crop.getSize() - 1) : new ItemStack(Ic2Items.get("resin"));
   }

   @Override
   public byte getSizeAfterHarvest(ICropTile crop) {
      return crop.getSize() == 4 ? (byte)(3 - java.util.concurrent.ThreadLocalRandom.current().nextInt(3)) : 1;
   }

   @Override
   public boolean onEntityCollision(ICropTile crop, Entity entity) {
      return false;
   }

   @Override
   public int growthDuration(ICropTile crop) {
      return crop.getSize() == 4 ? 400 : 100;
   }
}
