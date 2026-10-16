package ic2.core.block.kineticgenerator;
import ic2.api.energy.tile.IKineticSource;
import ic2.core.*;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.*;
import ic2.core.fluid.*;
import ic2.core.ref.*;
import ic2.core.upgrade.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import java.util.*;
public class TileEntitySteamKineticGenerator extends TileEntityInventory implements IHasGui,FluidMachine,IKineticSource,IUpgradableBlock {
    public final FluidTank steamTank=new FluidTank(21000,f->f.is(Ic2Fluids.STEAM.get())||f.is(Ic2Fluids.SUPERHEATED_STEAM.get())){protected void onContentsChanged(){setChanged();}};
    public final FluidTank waterTank=new FluidTank(1000,f->f.is(Ic2Fluids.DISTILLED_WATER.get())||f.is(Fluids.WATER)){protected void onContentsChanged(){setChanged();}};
    public final FluidTank exhaustTank=new FluidTank(21000,f->f.is(Ic2Fluids.STEAM.get())){protected void onContentsChanged(){setChanged();}};
    private final FluidTank[] tanks={steamTank,waterTank};
    public final InvSlot turbineSlot=new InvSlot(this,"turbine",InvSlot.Access.I,1){public boolean accepts(ItemStack s){return s.is(Ic2Items.STEAM_TURBINE.get());}};
    public final InvSlotUpgrade upgrades=new InvSlotUpgrade(this,"upgrade",1);
    public int condensation,updateTicker,kUoutput,available;
    public boolean hotExhaust;
    public TileEntitySteamKineticGenerator(BlockPos p,BlockState s){super(Ic2BlockEntities.STEAM_KINETIC_GENERATOR.get(),p,s);turbineSlot.setStackSizeLimit(1);}
    public FluidTank[] tanks(){return tanks;}
    public boolean canInput(Direction d){return d==null||d!=getFacing()&&d!=getFacing().getOpposite();}
    public boolean canOutput(Direction d){return canInput(d);}
    @Override public boolean isFluidValid(int i,FluidStack f){return tanks[i].isFluidValid(f);}
    @Override public int fill(FluidStack f,FluidAction a){return f.is(Fluids.WATER)||f.is(Ic2Fluids.DISTILLED_WATER.get())?waterTank.fill(f,a):steamTank.fill(f,a);}
    public int rate(){int n=steamTank.getFluidAmount();return n>18000?1000:n>16000?800:n>12000?600:n>8000?400:n>4000?200:n>2000?100:n>1000?50:n>800?40:n>600?30:n>400?20:n>10?10:0;}
    public boolean work(){
        kUoutput=available=0;if(turbineSlot.isEmpty()||waterTank.getFluidAmount()>=1000||!exhaustTank.isEmpty())return false;
        int amount=rate();if(amount<=0)return false;boolean hot=steamTank.getFluid().is(Ic2Fluids.SUPERHEATED_STEAM.get());
        int exhaust=hot?amount:amount*9/10;steamTank.drain(amount,FluidAction.EXECUTE);hotExhaust=hot;
        exhaustTank.fill(new FluidStack(Ic2Fluids.STEAM.get(),exhaust),FluidAction.EXECUTE);
        if(!hot)condensation+=amount;
        while(condensation>=1000&&waterTank.fill(new FluidStack(Ic2Fluids.DISTILLED_WATER.get(),1),FluidAction.SIMULATE)==1){waterTank.fill(new FluidStack(Ic2Fluids.DISTILLED_WATER.get(),1),FluidAction.EXECUTE);condensation-=1000;}
        kUoutput=(int)(amount*(hot?4:2)*(1-waterTank.getFluidAmount()/1000f));available=kUoutput;
        if(++updateTicker>=20){updateTicker=0;var rotor=turbineSlot.get();rotor.setDamageValue(rotor.getDamageValue()+(hot?1:2));if(rotor.getDamageValue()>=rotor.getMaxDamage())turbineSlot.put(ItemStack.EMPTY);}
        setChanged();return true;
    }
    public int exhaust(){int moved=0;for(var d:Direction.values()){
        var be=ic2.core.util.Util.loadedBlockEntity(level,worldPosition.relative(d));
        if(!(be instanceof ic2.core.block.machine.tileentity.TileEntityCondenser)&&!(hotExhaust&&be instanceof TileEntitySteamKineticGenerator))continue;
        var dst=level.getCapability(Capabilities.FluidHandler.BLOCK,be.getBlockPos(),d.getOpposite());if(dst!=null)moved+=FluidMachine.transfer(exhaustTank,dst,exhaustTank.getFluidAmount());
    }return moved;}
    protected void updateEntityServer(){exhaust();setActive(work());exhaust();var u=upgrades.get();if(!u.isEmpty()&&u.getItem() instanceof IUpgradeItem i&&i.onTick(u,this))setChanged();if(!exhaustTank.isEmpty()&&level.random.nextInt(10)==0)new ExplosionIC2(level,null,worldPosition.getX()+0.5,worldPosition.getY()+0.5,worldPosition.getZ()+0.5,1,1,ExplosionIC2.Type.Heat,null,0).doExplosion();}
    public int maxrequestkineticenergyTick(Direction d){return d==getFacing()?available:0;}
    public int requestkineticenergy(Direction d,int n){if(d!=getFacing()||n<=0)return 0;int out=Math.min(n,available);available-=out;return out;}
    @Override public boolean wrenchCanSetFacing(net.minecraft.world.entity.player.Player p,Direction d){return d.getAxis()!=Direction.Axis.Y&&d!=getFacing();}
    @Override public ContainerBase<?> getGuiContainer(int id,Inventory i){return new ic2.core.block.machine.container.ContainerSteamMachines.Turbine(id,i,this);}
    @Override public void readFromNBT(CompoundTag n,HolderLookup.Provider r){super.readFromNBT(n,r);steamTank.readFromNBT(r,n.getCompound("steamTank"));waterTank.readFromNBT(r,n.getCompound("waterTank"));exhaustTank.readFromNBT(r,n.getCompound("exhaustTank"));condensation=Math.max(0,n.getInt("condensation"));updateTicker=Math.max(0,n.getInt("updateTicker"));hotExhaust=n.getBoolean("hotExhaust");}
    @Override public void writeToNBT(CompoundTag n,HolderLookup.Provider r){super.writeToNBT(n,r);n.put("steamTank",steamTank.writeToNBT(r,new CompoundTag()));n.put("waterTank",waterTank.writeToNBT(r,new CompoundTag()));n.put("exhaustTank",exhaustTank.writeToNBT(r,new CompoundTag()));n.putInt("condensation",condensation);n.putInt("updateTicker",updateTicker);n.putBoolean("hotExhaust",hotExhaust);}
    public double getEnergy(){return 40;}public boolean useEnergy(double n){return true;}
    public Set<UpgradableProperty> getUpgradableProperties(){return EnumSet.of(UpgradableProperty.ItemConsuming,UpgradableProperty.FluidConsuming,UpgradableProperty.FluidProducing);}
}
