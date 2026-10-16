package ic2.core.block.misc;

import ic2.core.item.ItemBooze;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Items;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Booze barrel: water, wheat and hops brew beer that ages through Brew, Youngster, Beer, Ale and Dragonblood (or spoils to
 * Black Stuff), sugar cane ferments to rum. It ages only while no treetap is fitted; the tapped side fills stone mugs.
 */
public class TileEntityBarrel extends BlockEntity {
    public int type, boozeAmount, age;
    public boolean detailed = true;
    public int treetapSide;
    public int hopsCount, wheatCount, solidRatio, hopsRatio, timeRatio;

    public TileEntityBarrel(BlockPos pos, BlockState state) { super(Ic2BlockEntities.BARREL.get(), pos, state); }

    public void set(int value) {
        type = ItemBooze.getTypeOfValue(value);
        if (type > 0) boozeAmount = ItemBooze.getAmountOfValue(value);
        if (type == 1) {
            detailed = false;
            hopsRatio = ItemBooze.getHopsRatioOfBeerValue(value);
            solidRatio = ItemBooze.getSolidRatioOfBeerValue(value);
            timeRatio = ItemBooze.getTimeRatioOfBeerValue(value);
        }
        if (type == 2) {
            detailed = true;
            age = timeNedForRum(boozeAmount) * ItemBooze.getProgressOfRumValue(value) / 100;
        }
        setChanged();
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider r) {
        super.loadAdditional(nbt, r);
        type = nbt.getByte("type");
        boozeAmount = nbt.getByte("waterCount");
        age = nbt.getInt("age");
        treetapSide = nbt.getByte("treetapSide");
        detailed = nbt.getBoolean("detailed");
        if (type == 1) {
            if (detailed) { hopsCount = nbt.getByte("hopsCount"); wheatCount = nbt.getByte("wheatCount"); }
            solidRatio = nbt.getByte("solidRatio");
            hopsRatio = nbt.getByte("hopsRatio");
            timeRatio = nbt.getByte("timeRatio");
        }
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider r) {
        super.saveAdditional(nbt, r);
        nbt.putByte("type", (byte)type);
        nbt.putByte("waterCount", (byte)boozeAmount);
        nbt.putInt("age", age);
        nbt.putByte("treetapSide", (byte)treetapSide);
        nbt.putBoolean("detailed", detailed);
        if (type == 1) {
            if (detailed) { nbt.putByte("hopsCount", (byte)hopsCount); nbt.putByte("wheatCount", (byte)wheatCount); }
            nbt.putByte("solidRatio", (byte)solidRatio);
            nbt.putByte("hopsRatio", (byte)hopsRatio);
            nbt.putByte("timeRatio", (byte)timeRatio);
        }
    }

    public void serverTick() {
        if (isEmpty() || treetapSide >= 2) return;
        age++;
        if (type == 1 && timeRatio < 5) {
            int x = timeRatio == 4 ? 6 : timeRatio;
            if (age >= 24000.0 * Math.pow(3, x)) { age = 0; timeRatio++; }
        }
        if (age % 20 == 0) setChanged();
    }

    public boolean isEmpty() { return type == 0 || boozeAmount <= 0; }

    /** Pours in water, wheat, hops or sugar cane; sneaking adds one at a time. */
    public boolean rightclick(Player player, net.minecraft.world.InteractionHand hand) {
        ItemStack cur = player.getItemInHand(hand);
        if (cur.isEmpty()) return false;
        boolean one = player.isShiftKeyDown();
        if (cur.is(Items.WATER_BUCKET)) {
            if (!detailed || boozeAmount + 1 > 32 || type > 1) return false;
            type = 1;
            if (!player.getAbilities().instabuild) player.setItemInHand(hand, new ItemStack(Items.BUCKET));
            boozeAmount++;
        } else if (cur.is(Ic2Items.WATER_CELL.get())) {
            if (!detailed || type > 1) return false;
            type = 1;
            int give = Math.min(one ? 1 : cur.getCount(), 32 - boozeAmount);
            if (give <= 0) return false;
            boozeAmount += give;
            cur.shrink(give);
        } else if (cur.is(Items.WHEAT) || cur.is(Ic2Items.get("hops"))) {
            if (!detailed || type > 1) return false;
            type = 1;
            boolean wheat = cur.is(Items.WHEAT);
            int give = Math.min(one ? 1 : cur.getCount(), 64 - (wheat ? wheatCount : hopsCount));
            if (give <= 0) return false;
            if (wheat) wheatCount += give; else hopsCount += give;
            cur.shrink(give);
            alterComposition();
        } else if (cur.is(Items.SUGAR_CANE)) {
            if (age > 600 || type > 0 && type != 2) return false;
            type = 2;
            int give = Math.min(one ? 1 : cur.getCount(), 32 - boozeAmount);
            if (give <= 0) return false;
            boozeAmount += give;
            cur.shrink(give);
        } else return false;
        setChanged();
        return true;
    }

    public void alterComposition() {
        var rand = level.random;
        if (timeRatio == 0) age = 0;
        if (timeRatio == 1) { if (rand.nextBoolean()) timeRatio = 0; else if (rand.nextBoolean()) timeRatio = 5; }
        if (timeRatio == 2 && rand.nextBoolean()) timeRatio = 5;
        if (timeRatio > 2) timeRatio = 5;
    }

    public boolean drainLiquid(int amount) {
        if (isEmpty() || amount > boozeAmount) return false;
        enforceUndetailed();
        if (type == 2) {
            int progress = age * 100 / timeNedForRum(boozeAmount);
            boozeAmount -= amount;
            age = progress / 100 * timeNedForRum(boozeAmount);
        } else boozeAmount -= amount;
        if (boozeAmount <= 0) {
            if (type == 1) hopsCount = wheatCount = hopsRatio = solidRatio = timeRatio = 0;
            type = 0;
            detailed = true;
            boozeAmount = 0;
        }
        setChanged();
        return true;
    }

    /** Fixes the recipe ratios once the first booze is drawn (or the barrel is taken off). */
    public void enforceUndetailed() {
        if (!detailed) return;
        detailed = false;
        if (type != 1) return;
        float hops = wheatCount > 0 ? (float)hopsCount / wheatCount : 10;
        if (hopsCount <= 0 && wheatCount <= 0) hops = 0;
        float solid = boozeAmount > 0 ? (float)(hopsCount + wheatCount) / boozeAmount : 10;
        if (hops <= 0.25f) hopsRatio = 0;
        if (hops > 0.25f && hops <= 0.33333334f) hopsRatio = 1;
        if (hops > 0.33333334f && hops <= 0.5f) hopsRatio = 2;
        if (hops > 0.5f && hops < 2) hopsRatio = 3;
        if (hops >= 2 && hops < 3) hopsRatio = 4;
        if (hops >= 3 && hops < 4) hopsRatio = 5;
        if (hops >= 4 && hops < 5) hopsRatio = 6;
        if (hops >= 5) timeRatio = 5;
        // the original's solid ratio 1 test can never be true (x <= 5/12 && x > 5/12), kept as is
        if (solid > 0.5f && solid < 1) solidRatio = 2;
        if (solid == 1) solidRatio = 3;
        if (solid > 1 && solid < 2) solidRatio = 4;
        if (solid >= 2 && solid < 2.4f) solidRatio = 5;
        if (solid >= 2.4f && solid < 4) solidRatio = 6;
        if (solid >= 4) timeRatio = 5;
    }

    public void setTreetapSide(int side) {
        treetapSide = side;
        if (level != null) {
            BlockState s = getBlockState();
            level.setBlock(worldPosition, s.setValue(BlockBarrel.TAP, BlockBarrel.Tap.of(side)), 3);
        }
        setChanged();
    }

    public boolean useTreetapOn(Player player, ItemStack treetap, Direction side) {
        if (side.getAxis() == Direction.Axis.Y || treetapSide >= 2) return false;
        setTreetapSide(side.get3DDataValue());
        if (!player.getAbilities().instabuild) treetap.shrink(1);
        return true;
    }

    public int calculateMetaValue() {
        if (isEmpty()) return 0;
        enforceUndetailed();
        if (type == 1) {
            int value = timeRatio;
            value = value << 3 | hopsRatio;
            value = value << 3 | solidRatio;
            value = value << 5 | boozeAmount - 1;
            return value << 2 | type;
        }
        if (type == 2) {
            int progress = Math.min(100, age * 100 / timeNedForRum(boozeAmount));
            int value = progress << 5 | boozeAmount - 1;
            return value << 2 | type;
        }
        return 0;
    }

    public int timeNedForRum(int amount) { return (int)(1200 * amount * Math.pow(0.95, amount - 1)); }
}
