package ic2.core.block.machine.tileentity;
import ic2.core.*;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.explosive.NukeConfig;
import ic2.core.ref.*;
import net.minecraft.core.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
public class TileEntityNuke extends TileEntityInventory implements IHasGui {
    public final InvSlot outsideSlot=new InvSlot(this,"outside",InvSlot.Access.NONE,1){public boolean accepts(ItemStack s){return s.is(Ic2Blocks.INDUSTRIAL_TNT.get().asItem());}};
    public final InvSlot insideSlot=new InvSlot(this,"inside",InvSlot.Access.NONE,1){public boolean accepts(ItemStack s){return s.is(Ic2Blocks.URANIUM_BLOCK.get().asItem())||s.is(Ic2Items.get("uranium_238"))||s.is(Ic2Items.get("uranium_235"))||s.is(Ic2Items.get("small_uranium_235"))||s.is(Ic2Items.get("plutonium"))||s.is(Ic2Items.get("small_plutonium"));}};
    private int radiationRange;
    public TileEntityNuke(BlockPos p,BlockState s){super(Ic2BlockEntities.NUKE.get(),p,s);}
    public int getRadiationRange(){return radiationRange;}
    public float getNukeExplosivePower(){
        radiationRange=0;if(outsideSlot.isEmpty()||!outsideSlot.accepts(outsideSlot.get()))return -1;
        int tnt=Math.min(64,outsideSlot.get().getCount());double power=5*Math.cbrt(tnt);var core=insideSlot.get();int count=Math.min(64,core.getCount());
        if(core.is(Ic2Items.get("uranium_238")))radiationRange=tnt;
        else if(core.is(Ic2Blocks.URANIUM_BLOCK.get().asItem()))radiationRange=tnt*6;
        else if(core.is(Ic2Items.get("small_uranium_235"))){radiationRange=tnt*2;if(tnt>=64)power+=Math.pow(count,1.6)/18;}
        else if(core.is(Ic2Items.get("uranium_235"))){radiationRange=tnt*2;if(tnt>=32)power+=.5*Math.pow(count,1.4);}
        else if(core.is(Ic2Items.get("small_plutonium"))){radiationRange=tnt*3;if(tnt>=32)power+=Math.pow(count,2)/18;}
        else if(core.is(Ic2Items.get("plutonium"))){radiationRange=tnt*4;if(tnt>=16)power+=.5*Math.pow(count,1.8);}
        return (float)Math.min(power,NukeConfig.POWER_LIMIT.get());
    }
    public void onIgnite(){outsideSlot.clear();insideSlot.clear();setChanged();}
    @Override public ContainerBase<?> getGuiContainer(int id,Inventory i){return new ic2.core.block.machine.container.ContainerNuke(id,i,this);}
    @Override public void onNeighborUpdate(BlockPos from){if(level!=null&&!level.isClientSide&&level.hasNeighborSignal(worldPosition))ic2.core.block.explosive.BlockNuke.prime(level,worldPosition,null,false);}
    @Override public void onLoaded(){super.onLoaded();onNeighborUpdate(worldPosition);}
}
