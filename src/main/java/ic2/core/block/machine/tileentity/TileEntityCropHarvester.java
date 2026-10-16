package ic2.core.block.machine.tileentity;

import ic2.core.ContainerBase;
import ic2.core.block.invslot.*;
import ic2.core.crop.TileEntityCrop;
import ic2.core.ref.*;
import ic2.core.upgrade.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import java.util.*;

public class TileEntityCropHarvester extends TileEntityElectricMachine implements IUpgradableBlock, ic2.core.IHasGui {
    public final InvSlotOutput contentSlot=new InvSlotOutput(this,"content",15);
    public final InvSlot analyzerSlot=new InvSlot(this,"analyzer",InvSlot.Access.NONE,1){public boolean accepts(ItemStack s){return s.is(Ic2Items.CROP_ANALYZER.get());}};
    public final InvSlotUpgrade upgradeSlot=new InvSlotUpgrade(this,"upgrade",1);
    public int scanX=-5,scanY=-1,scanZ=-5;
    public TileEntityCropHarvester(BlockPos p,BlockState s){super(Ic2BlockEntities.CROP_HARVESTER.get(),p,s,10000,1);}
    @Override protected void updateEntityServer(){
        super.updateEntityServer();var u=upgradeSlot.get();if(!u.isEmpty()&&u.getItem() instanceof IUpgradeItem upgrade&&upgrade.onTick(u,this))setChanged();
        if(energy>=201)scan();
    }
    public void scan(){
        if(++scanX>5){scanX=-5;if(++scanZ>5){scanZ=-5;if(++scanY>1)scanY=-1;}}
        energy--;setChanged();setActive(false);
        boolean space=false;for(int i=0;i<contentSlot.size();i++){var s=contentSlot.get(i);if(s.isEmpty()||s.getCount()<s.getMaxStackSize()){space=true;break;}}
        if(space&&ic2.core.util.Util.loadedBlockEntity(level,worldPosition.offset(scanX,scanY,scanZ)) instanceof TileEntityCrop c){
            var drops=c.harvest_automated(!analyzerSlot.isEmpty());
            if(drops!=null){for(var s:drops)if(!s.isEmpty()){int left=contentSlot.add(s);if(left>0)net.minecraft.world.level.block.Block.popResource(level,worldPosition,s.copyWithCount(left));energy-=analyzerSlot.isEmpty()?100:200;}c.updateState();setActive(true);}
        }
    }
    @Override public ContainerBase<?> getGuiContainer(int id,Inventory i){return new ic2.core.crop.ContainerCrops.Harvester(id,i,this);}
    public double getEnergy(){return energy;}public boolean useEnergy(double n){if(energy<n)return false;energy-=n;return true;}
    public Set<UpgradableProperty> getUpgradableProperties(){return EnumSet.of(UpgradableProperty.ItemProducing);}
}
