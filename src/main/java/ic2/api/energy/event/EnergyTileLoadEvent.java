package ic2.api.energy.event;

import ic2.api.energy.tile.IEnergyTile;

/** Post on NeoForge.EVENT_BUS (server side) once the tile is placed or loaded. */
public class EnergyTileLoadEvent extends EnergyTileEvent {
    public EnergyTileLoadEvent(IEnergyTile energyTile) { super(energyTile); }
}
