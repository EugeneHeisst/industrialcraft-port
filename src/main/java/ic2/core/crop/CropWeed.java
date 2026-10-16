package ic2.core.crop;

import ic2.api.crops.ICropTile;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class CropWeed extends Ic2CropCard {
   @Override
   public String name() {
      return "weed";
   }

   @Override
   public int tier() {
      return 0;
   }

   @Override
   public int stat(int n) {
      switch (n) {
         case 0:
            return 0;
         case 1:
            return 0;
         case 2:
            return 1;
         case 3:
            return 0;
         case 4:
            return 5;
         default:
            return 0;
      }
   }

   @Override
   public String[] attributes() {
      return new String[]{"Weed", "Bad"};
   }

   @Override
   public int maxSize() {
      return 5;
   }

   @Override
   public int getOptimalHavestSize(ICropTile crop) {
      return 1;
   }

   @Override
   public boolean canGrow(ICropTile crop) {
      return crop.getSize() < 5;
   }

   @Override
   public boolean leftclick(ICropTile crop, Player player) {
      return false;
   }

   @Override
   public boolean canBeHarvested(ICropTile crop) {
      return false;
   }

   @Override
   public ItemStack getGain(ICropTile crop) {
      return null;
   }

   @Override
   public int growthDuration(ICropTile crop) {
      return 300;
   }

   @Override
   public boolean onEntityCollision(ICropTile crop, Entity entity) {
      return false;
   }
}
