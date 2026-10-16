package ic2.core.block.machine.tileentity;
import ic2.api.item.ElectricItem;
import ic2.core.*;
import ic2.core.block.invslot.*;
import ic2.core.ref.*;
import ic2.core.upgrade.*;
import ic2.core.util.OreValues;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import java.util.*;
/** Layer-by-layer remote miner: OD 17x17 / OV 33x33, 512 EU per block and 64 scanner EU per examined position. */
public class TileEntityAdvMiner extends TileEntityElectricMachine implements IHasGui,IUpgradableBlock {
    public final InvSlot scannerSlot=new InvSlot(this,"scanner",InvSlot.Access.IO,1,InvSlot.InvSide.BOTTOM){public boolean accepts(ItemStack s){return s.is(Ic2Items.OD_SCANNER.get())||s.is(Ic2Items.OV_SCANNER.get());}};
    public final InvSlotUpgrade upgradeSlot=new InvSlotUpgrade(this,"upgrade",4);
    public final InvSlot ListSlot=new InvSlot(this,"list",InvSlot.Access.NONE,15);
    public boolean blacklist=true,silktouch;
    public int minelayer=Integer.MAX_VALUE,xcounter=99,zcounter=99,ticker;
    public final int energyConsume=512,defaultTier=3,workTick=20;
    public TileEntityAdvMiner(BlockPos p,BlockState s){super(Ic2BlockEntities.ADV_MINER.get(),p,s,4000000,3,false);scannerSlot.setStackSizeLimit(1);}
    @Override public void onLoaded(){super.onLoaded();if(minelayer==Integer.MAX_VALUE)minelayer=worldPosition.getY()-1;setUpgradestat();}
    public void setUpgradestat(){upgradeSlot.onChanged();setTier(Math.min(32,defaultTier+upgradeSlot.extraTier));}
    public int range(){return scannerSlot.get().is(Ic2Items.OD_SCANNER.get())?8:16;}
    protected void updateEntityServer(){super.updateEntityServer();if(!scannerSlot.isEmpty()){double charged=ElectricItem.manager.charge(scannerSlot.get(),energy,2,false,false);if(charged>0){energy-=charged;setChanged();}}setUpgradestat();setActive(work());}
    public boolean work(){
        if(energy<energyConsume||upgradeSlot.getRedstoneInput(level.getBestNeighborSignal(worldPosition))>0||minelayer<level.getMinBuildHeight()||scannerSlot.isEmpty()||!ElectricItem.manager.canUse(scannerSlot.get(),64))return false;
        if(++ticker<workTick){setChanged();return true;}ticker=0;
        int radius=range(),budget=Math.min(4096,5*(upgradeSlot.augmentation+1));
        for(int n=0;n<budget&&minelayer>=level.getMinBuildHeight();n++){
            if(xcounter==99)xcounter=-radius;if(zcounter==99)zcounter=-radius;
            if(xcounter>radius){minelayer--;xcounter=zcounter=-radius;if(minelayer<level.getMinBuildHeight()){setChanged();return false;}}
            BlockPos target=new BlockPos(worldPosition.getX()-xcounter,minelayer,worldPosition.getZ()-zcounter);
            // Pause at an unloaded column rather than force loading or silently skipping it.
            if(!level.hasChunkAt(target))return false;
            if(!ElectricItem.manager.use(scannerSlot.get(),64,null))return false;
            if(++zcounter>radius){zcounter=-radius;xcounter++;}setChanged();
            if(mine(target))return true;
        }
        return minelayer>=level.getMinBuildHeight();
    }
    private ItemStack miningTool(){var tool=new ItemStack(Items.NETHERITE_PICKAXE);if(silktouch)tool.enchant(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH),1);return tool;}
    private boolean eligible(BlockPos pos,BlockState state){return !state.isAir()&&state.getFluidState().isEmpty()&&state.getDestroySpeed(level,pos)>=0&&(!state.hasBlockEntity()||OreValues.isValuable(state));}
    private boolean allowed(List<ItemStack> drops){if(drops.isEmpty())return false;boolean match=false;for(int i=0;i<ListSlot.size();i++)if(!ListSlot.get(i).isEmpty()&&ListSlot.get(i).is(drops.getFirst().getItem())){match=true;break;}return blacklist?!match:match;}
    public boolean mine(BlockPos target){
        if(!(level instanceof ServerLevel server)||energy<energyConsume||!level.hasChunkAt(target))return false;
        var state=level.getBlockState(target);if(!eligible(target,state))return false;
        var tool=miningTool();var actor=FakePlayerFactory.getMinecraft(server);
        var drops=Block.getDrops(state,server,target,level.getBlockEntity(target),actor,tool);if(!allowed(drops))return false;
        if(NeoForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level,target,state,actor)).isCanceled())return false;
        if(!level.removeBlock(target,false))return false;
        energy-=energyConsume;for(var drop:drops)distribute(drop);setChanged();return true;
    }
    private void distribute(ItemStack stack){for(var side:Direction.values()){var p=worldPosition.relative(side);if(!level.hasChunkAt(p))continue;var handler=level.getCapability(Capabilities.ItemHandler.BLOCK,p,side.getOpposite());if(handler!=null)stack=ItemHandlerHelper.insertItemStacked(handler,stack,false);if(stack.isEmpty())return;}Containers.dropItemStack(level,worldPosition.getX()+.5,worldPosition.getY()+1,worldPosition.getZ()+.5,stack);}
    public void event(int n){if(getActive())return;switch(n){case 0-> {minelayer=worldPosition.getY()-1;xcounter=zcounter=99;ticker=0;}case 1->blacklist=!blacklist;case 2->silktouch=!silktouch;default->{return;}}setChanged();}
    public int getminelayer(){return minelayer;}
    public ContainerBase<?> getGuiContainer(int id,Inventory i){return new ic2.core.block.machine.container.ContainerAdvMiner(id,i,this);}
    public double getEnergy(){return energy;}public boolean useEnergy(double n){if(n<0||energy<n)return false;energy-=n;return true;}
    public Set<UpgradableProperty> getUpgradableProperties(){return EnumSet.of(UpgradableProperty.Augmentable,UpgradableProperty.RedstoneSensitive,UpgradableProperty.Transformer);}
    @Override public void readFromNBT(CompoundTag n,HolderLookup.Provider r){super.readFromNBT(n,r);minelayer=n.contains("minelayer")?n.getInt("minelayer"):Integer.MAX_VALUE;xcounter=n.contains("xcounter")?n.getInt("xcounter"):99;zcounter=n.contains("zcounter")?n.getInt("zcounter"):99;blacklist=!n.contains("blacklist")||n.getBoolean("blacklist");silktouch=n.getBoolean("silktouch");ticker=Math.max(0,Math.min(workTick-1,n.getInt("ticker")));}
    @Override public void writeToNBT(CompoundTag n,HolderLookup.Provider r){super.writeToNBT(n,r);n.putInt("minelayer",minelayer);n.putInt("xcounter",xcounter);n.putInt("zcounter",zcounter);n.putBoolean("blacklist",blacklist);n.putBoolean("silktouch",silktouch);n.putInt("ticker",ticker);}
}
