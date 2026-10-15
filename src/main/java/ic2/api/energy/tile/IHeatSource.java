package ic2.api.energy.tile;

import net.minecraft.core.Direction;

public interface IHeatSource {
    int maxrequestHeatTick(Direction directionFrom);
    int requestHeat(Direction directionFrom, int requestheat);
}
