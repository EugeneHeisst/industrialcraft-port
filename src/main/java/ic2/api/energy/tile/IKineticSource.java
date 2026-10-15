package ic2.api.energy.tile;

import net.minecraft.core.Direction;

public interface IKineticSource {
    int maxrequestkineticenergyTick(Direction directionFrom);
    int requestkineticenergy(Direction directionFrom, int requestkineticenergy);
}
