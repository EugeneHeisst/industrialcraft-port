package ic2.core.crop;
import ic2.api.crops.*;
import ic2.core.block.TileEntityBlock;
import ic2.core.ref.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import java.util.*;

/** Original IC2 growth, crossing weights and harvest probabilities on modern world/block APIs. */
public class TileEntityCrop extends TileEntityBlock implements ICropTile {
    private CropCard crop;
    public int size, statGrowth, statGain, statResistance, scanLevel, growthPoints;
    public int nutrientStorage, waterStorage, exStorage;
    public boolean upgraded;
    private int ticker;
    private CompoundTag customData = new CompoundTag();
    public static final int tickRate = 256;
    public TileEntityCrop(BlockPos p, BlockState s) { super(Ic2BlockEntities.CROP.get(),p,s); ticker = Math.floorMod(p.hashCode(), tickRate); }
    @Override protected void updateEntityServer() {
        if (++ticker % tickRate == 0) { cropTick(); updateState(); }
    }
    public void cropTick() {
        if (crop == null) {
            if (upgraded && attemptCrossing()) return;
            if (level.random.nextInt(100) == 0 && !hasEx()) { reset(); crop = Crops.weed; size = 1; }
            else { if (exStorage > 0 && level.random.nextInt(10)==0) exStorage--; return; }
        }
        crop.tick(this);
        if (crop != null && crop.canGrow(this)) {
            int rate = calcGrowthRate();
            if (crop == null) return;
            growthPoints += rate;
            if (growthPoints >= crop.growthDuration(this)) { growthPoints=0; size++; }
        }
        if (nutrientStorage>0) nutrientStorage--;
        if (waterStorage>0) waterStorage--;
        if (crop!=null && crop.isWeed(this) && level.random.nextInt(50)-statGrowth<=2) generateWeed();
    }
    public boolean hasEx() { if(exStorage<=0)return false; exStorage=Math.max(0,exStorage-5); return true; }
    public void generateWeed() {
        BlockPos p=worldPosition.relative(Direction.from2DDataValue(level.random.nextInt(4)));
        if(ic2.core.util.Util.loadedBlockEntity(level,p) instanceof TileEntityCrop other &&
            (other.crop==null || !other.crop.isWeed(other) && level.random.nextInt(32)>=other.statResistance && !other.hasEx())) {
            int growth=Math.max(statGrowth,other.statGrowth); if(growth<31 && level.random.nextBoolean())growth++;
            other.reset(); other.crop=Crops.weed; other.size=1; other.statGrowth=growth; other.updateState();
        }
    }
   public boolean attemptCrossing() {
      if (level.random.nextInt(3) != 0) {
         return false;
      } else {
         List<TileEntityCrop> cropTes = new ArrayList<>(4);
         this.askCropJoinCross(worldPosition.getX() - 1, worldPosition.getY(), worldPosition.getZ(), cropTes);
         this.askCropJoinCross(worldPosition.getX() + 1, worldPosition.getY(), worldPosition.getZ(), cropTes);
         this.askCropJoinCross(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ() - 1, cropTes);
         this.askCropJoinCross(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ() + 1, cropTes);
         if (cropTes.size() < 2) {
            return false;
         } else {
            CropCard[] crops = Crops.instance.getCrops().toArray(new CropCard[0]);
            if (crops.length == 0) {
               return false;
            } else {
               int[] ratios = new int[crops.length];
               int total = 0;

               for (int i = 0; i < ratios.length; i++) {
                  CropCard crop = crops[i];
                  if (crop.canGrow(this)) {
                     for (TileEntityCrop te : cropTes) {
                        total += this.calculateRatioFor(crop, te.getCrop());
                     }
                  }

                  ratios[i] = total;
               }

               if (total <= 0) return false;
               int search = level.random.nextInt(total);
               int min = 0;
               int max = ratios.length - 1;

               while (min < max) {
                  int cur = (min + max) / 2;
                  int value = ratios[cur];
                  if (search < value) {
                     max = cur;
                  } else {
                     min = cur + 1;
                  }
               }

               assert min == max;

               assert min >= 0 && min < ratios.length;

               assert ratios[min] > search;

               assert min == 0 || ratios[min - 1] <= search;

               this.upgraded = false;
               this.crop = crops[min];
               this.size = 1;
               this.statGrowth = 0;
               this.statResistance = 0;
               this.statGain = 0;

               for (TileEntityCrop te : cropTes) {
                  this.statGrowth = this.statGrowth + te.statGrowth;
                  this.statResistance = this.statResistance + te.statResistance;
                  this.statGain = this.statGain + te.statGain;
               }

               int count = cropTes.size();
               this.statGrowth /= count;
               this.statResistance /= count;
               this.statGain /= count;
               this.statGrowth = this.statGrowth + (level.random.nextInt(1 + 2 * count) - count);
               this.statGain = this.statGain + (level.random.nextInt(1 + 2 * count) - count);
               this.statResistance = this.statResistance + (level.random.nextInt(1 + 2 * count) - count);
               this.statGrowth = net.minecraft.util.Mth.clamp(this.statGrowth, 0, 31);
               this.statGain = net.minecraft.util.Mth.clamp(this.statGain, 0, 31);
               this.statResistance = net.minecraft.util.Mth.clamp(this.statResistance, 0, 31);
               updateState();
               return true;
            }
         }
      }
   }

   public int calculateRatioFor(CropCard newCrop, CropCard oldCrop) {
      if (newCrop == oldCrop) {
         return 500;
      } else {
         int value = 0;

         for (int i = 0; i < 5; i++) {
            int delta = Math.abs(newCrop.stat(i) - oldCrop.stat(i));
            value += -delta + 2;
         }

         for (String attributeNew : newCrop.attributes()) {
            for (String attributeOld : oldCrop.attributes()) {
               if (attributeNew.equalsIgnoreCase(attributeOld)) {
                  value += 5;
               }
            }
         }

         int diff = newCrop.tier() - oldCrop.tier();
         if (diff > 1) {
            value -= 2 * diff;
         }

         if (diff < -3) {
            value -= -diff;
         }

         return Math.max(value, 0);
      }
   }

   public void askCropJoinCross(int x, int y, int z, List<TileEntityCrop> crops) {
      net.minecraft.world.level.block.entity.BlockEntity te = ic2.core.util.Util.loadedBlockEntity(level, new BlockPos(x, y, z));
      if (te instanceof TileEntityCrop) {
         TileEntityCrop sideCrop = (TileEntityCrop)te;
         CropCard neighborCrop = sideCrop.getCrop();
         if (neighborCrop != null) {
            if (neighborCrop.canGrow(this) && neighborCrop.canCross(sideCrop)) {
               int base = 4;
               if (sideCrop.statGrowth >= 16) {
                  base++;
               }

               if (sideCrop.statGrowth >= 30) {
                  base++;
               }

               if (sideCrop.statResistance >= 28) {
                  base += 27 - sideCrop.statResistance;
               }

               if (base >= level.random.nextInt(20)) {
                  crops.add(sideCrop);
               }
            }
         }
      }
   }

   @Override
   public ItemStack[] harvest_automated(boolean optimal) {
      if (this.crop == null) {
         return null;
      } else if (!this.crop.canBeHarvested(this)) {
         return null;
      } else if (optimal && this.size != this.crop.getOptimalHavestSize(this)) {
         return null;
      } else {
         double chance = (double)this.crop.dropGainChance();
         chance *= Math.pow(1.03, (double)this.statGain);
         int dropCount = (int)Math.max(0L, Math.round(level.random.nextGaussian() * chance * 0.6827 + chance));
         ItemStack[] ret = new ItemStack[dropCount];

         for (int i = 0; i < dropCount; i++) {
            ret[i] = this.crop.getGain(this);
            if (ret[i] == null) ret[i] = ItemStack.EMPTY;
            if (!ret[i].isEmpty() && level.random.nextInt(100) <= this.statGain) {
               ret[i].grow(1);
            }
         }

         this.size = this.crop.getSizeAfterHarvest(this);
         updateState();
         return ret;
      }
   }

   @Override
   public boolean harvest(boolean manual) {
      ItemStack[] drops = this.harvest_automated(false);
      if (drops == null) {
         return false;
      } else {
         if (!level.isClientSide && drops.length > 0) {
            for (ItemStack drop : drops) {
               drop(drop);
            }
         }

         return true;
      }
   }

   public int calcGrowthRate() {
      if (this.crop == null) {
         return 0;
      } else {
         int base = 3 + level.random.nextInt(7) + this.statGrowth;
         int need = (this.crop.tier() - 1) * 4 + this.statGrowth + this.statGain + this.statResistance;
         if (need < 0) {
            need = 0;
         }

         int have = this.crop.weightInfluences(this, (float)this.getHumidity(), (float)this.getNutrients(), (float)this.getAirQuality()) * 5;
         if (have >= need) {
            base = base * (100 + (have - need)) / 100;
         } else {
            int neg = (need - have) * 4;
            if (neg > 100 && level.random.nextInt(32) > this.statResistance) {
               this.reset();
               base = 0;
            } else {
               base = base * (100 - neg) / 100;
               if (base < 0) {
                  base = 0;
               }
            }
         }

         return base;
      }
   }


    @Override public boolean pick(boolean manual) {
        if(crop==null)return false;
        boolean bonus=harvest(false);
        float first=crop.dropSeedChance(this)*(float)Math.pow(1.1,statResistance);
        int count=0;
        if(bonus) {
            if(level.random.nextFloat()<=(first+1)*0.8f)count++;
            float chance=(crop.dropSeedChance(this)+statGrowth/100f)*(manual?1:0.8f)*(float)Math.pow(0.95,Math.max(0,statGain-23));
            if(level.random.nextFloat()<=chance)count++;
        } else if(level.random.nextFloat()<=first*1.5f)count++;
        for(int i=0;i<count;i++)drop(crop.getSeeds(this));
        reset(); return true;
    }
    public void drop(ItemStack stack) { if(stack!=null&&!stack.isEmpty()&&!level.isClientSide)Block.popResource(level,worldPosition,stack); }
    public boolean tryPlantIn(CropCard c,int stage,int growth,int gain,int resistance,int scan) {
        if(c==null||c==Crops.weed||crop!=null||upgraded||!c.canGrow(this))return false;
        reset(); crop=c; size=Math.max(1,Math.min(stage,c.maxSize())); statGrowth=clamp(growth); statGain=clamp(gain); statResistance=clamp(resistance); scanLevel=Math.max(0,Math.min(scan,4)); updateState(); return true;
    }
    private static int clamp(int n) { return Math.max(0,Math.min(n,31)); }
    public boolean applyFertilizer(boolean manual) { if(nutrientStorage>=100)return false; nutrientStorage+=manual?100:90; updateState(); return true; }
    public boolean applyWeedEx(boolean manual) {
        if((manual && exStorage>=100)||exStorage>=150)return false;
        exStorage+=50;
        if(crop!=null&&crop.isWeed(this)&&exStorage>=75&&level.random.nextInt(manual?5:3)==0) {
            int n=level.random.nextInt(5); if(n==0)statGrowth=Math.max(0,statGrowth-1); if(n<=1)statGain=Math.max(0,statGain-1); statResistance=Math.max(0,statResistance-1);
        }
        updateState(); return true;
    }
    @Override public byte getHumidity() {
        int n=Crops.instance.getHumidityBiomeBonus(level.getBiome(worldPosition));
        var below=level.getBlockState(worldPosition.below()); if(below.hasProperty(FarmBlock.MOISTURE)&&below.getValue(FarmBlock.MOISTURE)>=7)n+=2;
        if(waterStorage>=5)n+=2; return (byte)(n+(waterStorage+24)/25);
    }
    @Override public byte getNutrients() {
        int n=Crops.instance.getNutrientBiomeBonus(level.getBiome(worldPosition));
        for(int i=2;i<5&&level.getBlockState(worldPosition.below(i)).is(Blocks.DIRT);i++)n++;
        return (byte)(n+(nutrientStorage+19)/20);
    }
    @Override public byte getAirQuality() {
        int n=Math.max(0,Math.min(4,(worldPosition.getY()-64)/15)),fresh=9;
        for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++) {
            var p=worldPosition.offset(x,0,z); if(level.getBlockState(p).isSolidRender(level,p)||ic2.core.util.Util.loadedBlockEntity(level,p) instanceof TileEntityCrop)fresh--;
        }
        return (byte)(n+fresh/2+(level.canSeeSky(worldPosition.above())?2:0));
    }
    @Override public boolean isBlockBelow(Block block) {
        if(crop==null)return false;
        for(int i=1;i<crop.getrootslength(this);i++){var s=level.getBlockState(worldPosition.below(i));if(s.isAir())return false;if(s.is(block))return true;}return false;
    }
    @Override public boolean isBlockBelow(String oreName) {
        String modern=oreName.startsWith("ore")?"ores/"+oreName.substring(3).toLowerCase(Locale.ROOT):oreName.startsWith("block")?"storage_blocks/"+oreName.substring(5).toLowerCase(Locale.ROOT):oreName;
        var tag=net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK,net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("c",modern));
        if(crop==null)return false;
        for(int i=1;i<crop.getrootslength(this);i++){var s=level.getBlockState(worldPosition.below(i));if(s.isAir())return false;if(s.is(tag))return true;}return false;
    }
    @Override public void reset() { crop=null; size=statGrowth=statGain=statResistance=scanLevel=growthPoints=0; customData=new CompoundTag(); upgraded=false; updateState(); }
    @Override public void updateState() {
        setChanged();
        if(level!=null&&!level.isClientSide) {
            var s=getBlockState(); String name=crop==null||!crop.owner().equals("IC2")?"empty":crop.name().toLowerCase(Locale.ROOT);
            if(s.hasProperty(BlockCrop.PLANT))level.setBlock(worldPosition,s.setValue(BlockCrop.PLANT,BlockCrop.Plant.of(name)).setValue(BlockCrop.SIZE,Math.max(1,Math.min(size,7))).setValue(BlockCrop.CROSS,upgraded),3);
            level.getChunkSource().getLightEngine().checkBlock(worldPosition);
            sendClientUpdate();
        }
    }
    @Override public CropCard getCrop(){return crop;}
    @Override public void setCrop(CropCard c){crop=c;updateState();}
    @Override public short getID(){return (short)Crops.instance.getIdFor(crop);}
    @Override public void setID(short id){setCrop(IC2Crops.getCropFromId(id));}
    @Override public byte getSize(){return (byte)size;}
    @Override public void setSize(byte n){size=n;updateState();}
    @Override public byte getGrowth(){return (byte)statGrowth;}
    @Override public void setGrowth(byte n){statGrowth=clamp(n);}
    @Override public byte getGain(){return (byte)statGain;}
    @Override public void setGain(byte n){statGain=clamp(n);}
    @Override public byte getResistance(){return (byte)statResistance;}
    @Override public void setResistance(byte n){statResistance=clamp(n);}
    @Override public byte getScanLevel(){return (byte)scanLevel;}
    @Override public void setScanLevel(byte n){scanLevel=Math.max(0,Math.min(4,n));setChanged();}
    @Override public CompoundTag getCustomData(){return customData;}
    @Override public int getNutrientStorage(){return nutrientStorage;}
    @Override public void setNutrientStorage(int n){nutrientStorage=Math.max(0,n);setChanged();}
    @Override public int getHydrationStorage(){return waterStorage;}
    @Override public void setHydrationStorage(int n){waterStorage=Math.max(0,n);setChanged();}
    @Override public int getWeedExStorage(){return exStorage;}
    @Override public void setWeedExStorage(int n){exStorage=Math.max(0,n);setChanged();}
    @Override public int getLightLevel(){return level.getMaxLocalRawBrightness(worldPosition);}
    @Override public Level getWorld(){return level;}
    @Override public BlockPos getLocation(){return worldPosition;}
    @Override public ItemStack generateSeeds(CropCard c,byte g,byte a,byte r,byte scan){return ic2.core.item.ItemCropSeed.create(c,g,a,r,scan);}
    @Override public ItemStack generateSeeds(short id,byte g,byte a,byte r,byte scan){return generateSeeds(IC2Crops.getCropFromId(id),g,a,r,scan);}
    @Override public void onNeighborUpdate(BlockPos p){if(crop!=null)crop.onNeighbourChange(this);}
    @Override public void readFromNBT(CompoundTag n,HolderLookup.Provider r) {
        crop=Crops.instance.getCropCard(n.getString("cropOwner"),n.getString("cropName"));
        size=n.getByte("size");statGrowth=clamp(n.getByte("statGrowth"));statGain=clamp(n.getByte("statGain"));statResistance=clamp(n.getByte("statResistance"));scanLevel=n.getByte("scanLevel");
        growthPoints=n.getInt("growthPoints");nutrientStorage=n.getInt("nutrientStorage");waterStorage=n.getInt("waterStorage");exStorage=n.getInt("exStorage");upgraded=n.getBoolean("upgraded");customData=n.getCompound("customData").copy();
    }
    @Override public void writeToNBT(CompoundTag n,HolderLookup.Provider r) {
        if(crop!=null){n.putString("cropOwner",crop.owner());n.putString("cropName",crop.name());}
        n.putByte("size",(byte)size);n.putByte("statGrowth",(byte)statGrowth);n.putByte("statGain",(byte)statGain);n.putByte("statResistance",(byte)statResistance);n.putByte("scanLevel",(byte)scanLevel);
        n.putInt("growthPoints",growthPoints);n.putInt("nutrientStorage",nutrientStorage);n.putInt("waterStorage",waterStorage);n.putInt("exStorage",exStorage);n.putBoolean("upgraded",upgraded);n.put("customData",customData.copy());
    }
    @Override protected void readClientData(CompoundTag n,HolderLookup.Provider r){readFromNBT(n,r);if(level!=null)level.getChunkSource().getLightEngine().checkBlock(worldPosition);}
    @Override protected void writeClientData(CompoundTag n,HolderLookup.Provider r){writeToNBT(n,r);}
}
