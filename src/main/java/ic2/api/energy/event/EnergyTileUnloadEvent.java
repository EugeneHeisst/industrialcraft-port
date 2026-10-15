package ic2.api.energy.event;

import ic2.api.energy.tile.IEnergyTile;

/** Post on NeoForge.EVENT_BUS (server side) before the tile is removed or unloaded. */
public class EnergyTileUnloadEvent extends EnergyTileEvent {
    public EnergyTileUnloadEvent(IEnergyTile energyTile) { super(energyTile); }
}
