package ic2.api.crops;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
public abstract class CropCard {
   private final String modId = "ic2";

   public abstract String name();

   public String owner() {
      return this.modId;
   }

   public String displayName() {
      return this.name();
   }

   public String discoveredBy() {
      return "unknown";
   }

   public String desc(int i) {
      String[] att = this.attributes();
      if (att == null || att.length == 0) {
         return "";
      } else if (i == 0) {
         String s = att[0];
         if (att.length >= 2) {
            s = s + ", " + att[1];
            if (att.length >= 3) {
               s = s + ",";
            }
         }

         return s;
      } else if (att.length < 3) {
         return "";
      } else {
         String s = att[2];
         if (att.length >= 4) {
            s = s + ", " + att[3];
         }

         return s;
      }
   }

   public int getrootslength(ICropTile crop) {
      return 1;
   }

   public abstract int tier();

   public abstract int stat(int var1);

   public abstract String[] attributes();

   public abstract int maxSize();

   public net.minecraft.resources.ResourceLocation texture(int size) {
      return net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(owner().toLowerCase(java.util.Locale.ROOT), "block/crop/" + name().toLowerCase(java.util.Locale.ROOT) + "." + Math.max(1, Math.min(size, maxSize())));
   }

   public int growthDuration(ICropTile crop) {
      return this.tier() * 200;
   }

   public abstract boolean canGrow(ICropTile var1);

   public int weightInfluences(ICropTile crop, float humidity, float nutrients, float air) {
      return (int)(humidity + nutrients + air);
   }

   public boolean canCross(ICropTile crop) {
      return crop.getSize() >= 3;
   }

   public boolean rightclick(ICropTile crop, Player player) {
      return crop.harvest(true);
   }

   public abstract int getOptimalHavestSize(ICropTile var1);

   public abstract boolean canBeHarvested(ICropTile var1);

   public float dropGainChance() {
      return (float)Math.pow(0.95, (double)this.tier());
   }

   public abstract ItemStack getGain(ICropTile var1);

   public byte getSizeAfterHarvest(ICropTile crop) {
      return 1;
   }

   public boolean leftclick(ICropTile crop, Player player) {
      return crop.pick(true);
   }

   public float dropSeedChance(ICropTile crop) {
      if (crop.getSize() == 1) {
         return 0.0F;
      } else {
         float base = 0.5F;
         if (crop.getSize() == 2) {
            base /= 2.0F;
         }

         for (int i = 0; i < this.tier(); i++) {
            base = (float)((double)base * 0.8);
         }

         return base;
      }
   }

   public ItemStack getSeeds(ICropTile crop) {
      return crop.generateSeeds(crop.getCrop(), crop.getGrowth(), crop.getGain(), crop.getResistance(), crop.getScanLevel());
   }

   public void onNeighbourChange(ICropTile crop) {
   }

   public int emitRedstone(ICropTile crop) {
      return 0;
   }

   public void onBlockDestroyed(ICropTile crop) {
   }

   public int getEmittedLight(ICropTile crop) {
      return 0;
   }

   public boolean onEntityCollision(ICropTile crop, Entity entity) {
      return entity instanceof LivingEntity ? ((LivingEntity)entity).isSprinting() : false;
   }

   public void tick(ICropTile crop) {
   }

   public boolean isWeed(ICropTile crop) {
      return crop.getSize() >= 2 && (crop.getCrop() == Crops.weed || crop.getGrowth() >= 24);
   }

   @Deprecated
   public final int getId() {
      return Crops.instance.getIdFor(this);
   }

}
