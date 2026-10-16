package ic2.core.crop;

import ic2.api.crops.ICropTile;
import ic2.core.IC2;
import ic2.core.ref.Ic2Items;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;

public class CropVenomilia extends Ic2CropCard {
   @Override
   public String name() {
      return "venomilia";
   }

   @Override
   public String discoveredBy() {
      return "raGan";
   }

   @Override
   public int tier() {
      return 3;
   }

   @Override
   public int stat(int n) {
      switch (n) {
         case 0:
            return 3;
         case 1:
            return 1;
         case 2:
            return 3;
         case 3:
            return 3;
         case 4:
            return 3;
         default:
            return 0;
      }
   }

   @Override
   public String[] attributes() {
      return new String[]{"Purple", "Flower", "Tulip", "Poison"};
   }

   @Override
   public int maxSize() {
      return 6;
   }

   @Override
   public boolean canGrow(ICropTile crop) {
      return crop.getSize() <= 4 && crop.getLightLevel() >= 12 || crop.getSize() == 5;
   }

   @Override
   public boolean canBeHarvested(ICropTile crop) {
      return crop.getSize() >= 4;
   }

   @Override
   public int getOptimalHavestSize(ICropTile crop) {
      return 5;
   }

   @Override
   public ItemStack getGain(ICropTile crop) {
      if (crop.getSize() == 5) {
         return new ItemStack(Ic2Items.get("grin_powder"), 1);
      } else {
         return crop.getSize() >= 4 ? new ItemStack(Items.PURPLE_DYE) : null;
      }
   }

   @Override
   public byte getSizeAfterHarvest(ICropTile crop) {
      return 3;
   }

   @Override
   public int growthDuration(ICropTile crop) {
      return crop.getSize() >= 3 ? 600 : 400;
   }

   @Override
   public boolean rightclick(ICropTile crop, Player player) {
      if (!player.isShiftKeyDown()) {
         this.onEntityCollision(crop, player);
      }

      return crop.harvest(true);
   }

   @Override
   public boolean leftclick(ICropTile crop, Player player) {
      if (!player.isShiftKeyDown()) {
         this.onEntityCollision(crop, player);
      }

      return crop.pick(true);
   }

   @Override
   public boolean onEntityCollision(ICropTile crop, Entity entity) {
      if (crop.getSize() == 5 && entity instanceof LivingEntity) {
         if (entity instanceof Player && ((Player)entity).isShiftKeyDown() && java.util.concurrent.ThreadLocalRandom.current().nextInt(50) != 0) {
            return super.onEntityCollision(crop, entity);
         }

         ((LivingEntity)entity).addEffect(new MobEffectInstance(MobEffects.POISON, (java.util.concurrent.ThreadLocalRandom.current().nextInt(10) + 5) * 20, 0));
         crop.setSize((byte)4);
         crop.updateState();
      }

      return super.onEntityCollision(crop, entity);
   }

   @Override
   public boolean isWeed(ICropTile crop) {
      return crop.getSize() == 5 && crop.getGrowth() >= 8;
   }
}
