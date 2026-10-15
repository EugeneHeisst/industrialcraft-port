package ic2.api.energy.tile;

import net.minecraft.core.Direction;

public interface IEnergySink extends IEnergyAcceptor {
    double getDemandedEnergy();
    /** Integer.MAX_VALUE means "any tier". */
    int getSinkTier();
    /** @return the amount that could not be accepted */
    double injectEnergy(Direction directionFrom, double amount, double voltage);
}
