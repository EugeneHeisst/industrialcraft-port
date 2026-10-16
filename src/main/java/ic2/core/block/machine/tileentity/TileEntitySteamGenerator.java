package ic2.core.block.machine.tileentity;
import ic2.api.energy.tile.IHeatSource;
import ic2.core.*;
import ic2.core.block.TileEntityInventory;
import ic2.core.fluid.*;
import ic2.core.ref.Ic2BlockEntities;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
/** Pressure-controlled boiler, 100 steam per mB water; hot steam at 220 bar / 374 C. */
public class TileEntitySteamGenerator extends TileEntityInventory implements IHasGui,FluidMachine {
    public final FluidTank waterTank=new FluidTank(10000,f->f.is(Fluids.WATER)||f.is(Ic2Fluids.DISTILLED_WATER.get())){protected void onContentsChanged(){setChanged();}};
    public final FluidTank steamTank=new FluidTank(100000){protected void onContentsChanged(){setChanged();}};
    private final FluidTank[] tanks={waterTank,steamTank};
    public int inputmb,outputmb,pressurevalve,calcification,heatinput;
    public float systemheat=20;
    public TileEntitySteamGenerator(BlockPos p,BlockState s){super(Ic2BlockEntities.STEAM_GENERATOR.get(),p,s);}
    public FluidTank[] tanks(){return tanks;}
    public boolean canOutput(Direction d){return false;}
    public int targetTemperature(){return 100+Math.round(pressurevalve/220f*100*2.74f);}
    public int heatPerMB(){return 100+Math.round(pressurevalve/220f*100);}
    private int pullHeat(int wanted){
        int left=wanted;for(var d:Direction.values()){if(left<=0)break;var n=ic2.core.util.Util.loadedBlockEntity(level,worldPosition.relative(d));if(n instanceof IHeatSource source)left-=source.requestHeat(d.getOpposite(),left);}return wanted-left;
    }
    public boolean produce(){
        outputmb=0;heatinput=0;if(calcification>=100000)return false;
        if(!steamTank.isEmpty())return false;
        int amount=Math.min(inputmb,waterTank.getFluidAmount());
        if(amount<=0){heatinput=pullHeat(1200);systemheat+=heatinput*0.0005f;setChanged();return heatinput>0;}
        int target=targetTemperature();
        if(systemheat<target){heatinput=pullHeat(1200);systemheat=Math.min(target,systemheat+heatinput*0.0005f);setChanged();return heatinput>0;}
        var fluid=target>=374&&systemheat>=374?Ic2Fluids.SUPERHEATED_STEAM.get():Ic2Fluids.STEAM.get();
        int room=steamTank.getCapacity()-steamTank.getFluidAmount();amount=Math.min(amount,room/100);if(amount<=0)return false;
        heatinput=pullHeat(amount*heatPerMB());int made=heatinput/heatPerMB();
        // Unused fractional heat stays in the boiler's temperature rather than creating partial water batches.
        systemheat+=(heatinput-made*heatPerMB())*0.0005f;
        if(made<=0){setChanged();return heatinput>0;}
        boolean ordinary=waterTank.getFluid().is(Fluids.WATER);waterTank.drain(made,FluidAction.EXECUTE);
        outputmb=made*100;steamTank.fill(new FluidStack(fluid,outputmb),FluidAction.EXECUTE);
        if(ordinary)calcification=Math.min(100000,calcification+1);
        if(systemheat>target)systemheat=Math.max(target,systemheat-made*0.1f);
        setChanged();return true;
    }
    public int distributeSteam(){int moved=0;for(var d:Direction.values())if(!steamTank.isEmpty()&&level.hasChunkAt(worldPosition.relative(d))){var dst=level.getCapability(Capabilities.FluidHandler.BLOCK,worldPosition.relative(d),d.getOpposite());if(dst!=null)moved+=FluidMachine.transfer(steamTank,dst,steamTank.getFluidAmount());}return moved;}
    @Override protected void updateEntityServer(){
        distributeSteam();boolean active=produce();distributeSteam();setActive(active);
        float ambient=20+level.getBiome(worldPosition).value().getBaseTemperature()*10;
        if(!active)systemheat=Math.max(ambient,systemheat-0.01f);
        if(systemheat>500){level.removeBlock(worldPosition,false);new ExplosionIC2(level,null,worldPosition.getX()+0.5,worldPosition.getY()+0.5,worldPosition.getZ()+0.5,10,0.01f,ExplosionIC2.Type.Heat,null,0).doExplosion();}
        else if(!steamTank.isEmpty()&&level.random.nextInt(10)==0)new ExplosionIC2(level,null,worldPosition.getX()+0.5,worldPosition.getY()+0.5,worldPosition.getZ()+0.5,1,1,ExplosionIC2.Type.Heat,null,0).doExplosion();
    }
    public void event(int event){if(event>=-2000&&event<=2000)inputmb=(int)Math.max(0,Math.min(1000,(long)inputmb+event));else pressurevalve=(int)Math.max(0,Math.min(300,(long)pressurevalve+event+(event>0?-2000:2000)));setChanged();}
    @Override public ContainerBase<?> getGuiContainer(int id,Inventory i){return new ic2.core.block.machine.container.ContainerSteamMachines.Boiler(id,i,this);}
    @Override public void readFromNBT(CompoundTag n,HolderLookup.Provider r){super.readFromNBT(n,r);waterTank.readFromNBT(r,n.getCompound("waterTank"));steamTank.readFromNBT(r,n.getCompound("steamTank"));inputmb=Math.max(0,Math.min(1000,n.getInt("inputmb")));pressurevalve=Math.max(0,Math.min(300,n.getInt("pressurevalve")));calcification=Math.max(0,n.getInt("calcification"));systemheat=n.getFloat("systemheat");if(!Float.isFinite(systemheat))systemheat=20;}
    @Override public void writeToNBT(CompoundTag n,HolderLookup.Provider r){super.writeToNBT(n,r);n.put("waterTank",waterTank.writeToNBT(r,new CompoundTag()));n.put("steamTank",steamTank.writeToNBT(r,new CompoundTag()));n.putInt("inputmb",inputmb);n.putInt("pressurevalve",pressurevalve);n.putInt("calcification",calcification);n.putFloat("systemheat",systemheat);}
}
