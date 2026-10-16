package ic2.core;
import ic2.api.recipe.IFluidHeatManager;
import ic2.core.fluid.HeatRecipes;
import net.minecraft.world.level.material.*;
import java.util.*;
public class FluidHeatManager implements IFluidHeatManager {
    private final Map<String,BurnProperty> map=new LinkedHashMap<>();
    public void addFluid(String name,int amount,int heat){if(amount<=0||heat<=0||map.containsKey(name))throw new IllegalArgumentException("Invalid or duplicate fluid heat recipe: "+name);map.put(name,new BurnProperty(amount,heat));}
    public BurnProperty getBurnProperty(Fluid f){if(f==null||f==Fluids.EMPTY)return null;for(var e:map.entrySet())if(HeatRecipes.resolve(e.getKey())==f)return e.getValue();return null;}
    public boolean acceptsFluid(Fluid f){return getBurnProperty(f)!=null;}
    public Set<Fluid> getAcceptedFluids(){var out=new LinkedHashSet<Fluid>();for(var name:map.keySet()){var f=HeatRecipes.resolve(name);if(f!=Fluids.EMPTY)out.add(f);}return Collections.unmodifiableSet(out);}
    public Map<String,BurnProperty> getBurnProperties(){return map;}
}
