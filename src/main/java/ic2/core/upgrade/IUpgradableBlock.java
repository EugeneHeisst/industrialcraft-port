package ic2.core.upgrade;

import java.util.Set;

public interface IUpgradableBlock {
    double getEnergy();
    boolean useEnergy(double amount);
    Set<UpgradableProperty> getUpgradableProperties();
}
