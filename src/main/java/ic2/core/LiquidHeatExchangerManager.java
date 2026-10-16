package ic2.core;
import ic2.api.recipe.*;
import ic2.core.fluid.HeatRecipes;
import net.minecraft.world.level.material.*;
import java.util.*;
public class LiquidHeatExchangerManager implements ILiquidHeatExchangerManager {
    private final boolean heatup;
    private final Map<String,HeatExchangeProperty> map=new LinkedHashMap<>();
    public LiquidHeatExchangerManager(boolean heatup){this.heatup=heatup;}
    public void addFluid(String input,String output,int hu){
        var in=HeatRecipes.resolve(input);var out=HeatRecipes.resolve(output);
        if(in==Fluids.EMPTY||out==Fluids.EMPTY||hu==0||map.containsKey(input))throw new IllegalArgumentException("Invalid or duplicate heat exchange: "+input+" -> "+output);
        map.put(input,new HeatExchangeProperty(out,Math.abs(hu)));
    }
    public HeatExchangeProperty getHeatExchangeProperty(Fluid f){if(f==null||f==Fluids.EMPTY)return null;for(var e:map.entrySet())if(HeatRecipes.resolve(e.getKey())==f)return e.getValue();return null;}
    public boolean acceptsFluid(Fluid f){return getHeatExchangeProperty(f)!=null;}
    public Set<Fluid> getAcceptedFluids(){var set=new LinkedHashSet<Fluid>();for(var name:map.keySet())set.add(HeatRecipes.resolve(name));return Collections.unmodifiableSet(set);}
    public Map<String,HeatExchangeProperty> getHeatExchangeProperties(){return map;}
    public ILiquidAcceptManager getSingleDirectionLiquidManager(){var self=this;return new ILiquidAcceptManager(){
        private ILiquidHeatExchangerManager reverse(){return heatup?Recipes.liquidCooldownManager:Recipes.liquidHeatupManager;}
        public boolean acceptsFluid(Fluid f){var p=self.getHeatExchangeProperty(f);return p!=null&&(reverse()==null||!reverse().acceptsFluid(p.outputFluid));}
        public Set<Fluid> getAcceptedFluids(){var set=new LinkedHashSet<Fluid>();for(var f:self.getAcceptedFluids())if(acceptsFluid(f))set.add(f);return Collections.unmodifiableSet(set);}
    };}
}
