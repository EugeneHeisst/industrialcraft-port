package ic2.api.energy.event;

import ic2.api.energy.tile.IEnergyTile;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.Event;

public class EnergyTileEvent extends Event {
    public final IEnergyTile energyTile;
    public final BlockEntity tile;
    public EnergyTileEvent(IEnergyTile energyTile) { this.energyTile = energyTile; this.tile = (BlockEntity)energyTile; }
}
