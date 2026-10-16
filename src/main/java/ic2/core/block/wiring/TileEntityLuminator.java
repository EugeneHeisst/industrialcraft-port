package ic2.core.block.wiring;
import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergySink;
import ic2.core.block.TileEntityBlock;
import ic2.core.ref.Ic2BlockEntities;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
public class TileEntityLuminator extends TileEntityBlock implements IEnergySink {
    public double energy;public boolean lswitch=true;public int ticker;private boolean registered;
    public TileEntityLuminator(BlockPos p,BlockState s){super(Ic2BlockEntities.LUMINATOR.get(),p,s);}
    @Override public void onLoaded(){super.onLoaded();if(!level.isClientSide&&!registered){EnergyNet.instance.addTile(this);registered=true;}}
    @Override public void onUnloaded(){if(registered&&level!=null&&!level.isClientSide){EnergyNet.instance.removeTile(this);registered=false;}super.onUnloaded();}
    protected void updateEntityServer(){if(ticker++%4==0){if(isActive())energy=Math.max(0,energy-1);setActive(isActive());setChanged();}}
    public boolean isActive(){return energy>0&&(lswitch!=level.hasNeighborSignal(worldPosition));}
    public double getDemandedEnergy(){return Math.max(0,2-energy);}public int getSinkTier(){return 1;}
    public double injectEnergy(Direction side,double amount,double voltage){if(amount<=0)return amount;double accepted=Math.min(amount,getDemandedEnergy());energy+=accepted;setChanged();return amount-accepted;}
    public boolean acceptsEnergyFrom(BlockEntity emitter,Direction side){return emitter instanceof TileEntityCable&&side==getFacing().getOpposite();}
    public boolean wrenchCanSetFacing(Player p,Direction side){return true;}public void setFacing(Direction side){lswitch=!lswitch;setChanged();}public boolean wrenchCanRemove(Player p){return false;}
    @Override public void readFromNBT(CompoundTag n,HolderLookup.Provider r){super.readFromNBT(n,r);energy=Math.max(0,Math.min(2,n.getDouble("energy")));lswitch=!n.contains("lswitch")||n.getBoolean("lswitch");ticker=Math.max(0,n.getInt("ticker"));}
    @Override public void writeToNBT(CompoundTag n,HolderLookup.Provider r){super.writeToNBT(n,r);n.putDouble("energy",energy);n.putBoolean("lswitch",lswitch);n.putInt("ticker",ticker);}
}
