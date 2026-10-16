package ic2.api.crops;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface ICropTile {
   CropCard getCrop();

   void setCrop(CropCard var1);

   @Deprecated
   short getID();

   @Deprecated
   void setID(short var1);

   byte getSize();

   void setSize(byte var1);

   byte getGrowth();

   void setGrowth(byte var1);

   byte getGain();

   void setGain(byte var1);

   byte getResistance();

   void setResistance(byte var1);

   byte getScanLevel();

   void setScanLevel(byte var1);

   CompoundTag getCustomData();

   int getNutrientStorage();

   void setNutrientStorage(int var1);

   int getHydrationStorage();

   void setHydrationStorage(int var1);

   int getWeedExStorage();

   void setWeedExStorage(int var1);

   byte getHumidity();

   byte getNutrients();

   byte getAirQuality();

   Level getWorld();

   BlockPos getLocation();

   int getLightLevel();

   boolean pick(boolean var1);

   boolean harvest(boolean var1);

   ItemStack[] harvest_automated(boolean var1);

   void reset();

   void updateState();

   boolean isBlockBelow(Block var1);

   boolean isBlockBelow(String var1);

   ItemStack generateSeeds(CropCard var1, byte var2, byte var3, byte var4, byte var5);

   @Deprecated
   ItemStack generateSeeds(short var1, byte var2, byte var3, byte var4, byte var5);
}
