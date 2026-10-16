package ic2.core.fluid;
import ic2.api.recipe.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.*;
import java.util.*;
/** Modern fluid identities with original API string aliases for addon compatibility. */
public final class HeatRecipes {
    public static Fluid resolve(String name){
        if(name.equals("ic2biogas"))return Ic2Fluids.BIOGAS.get();
        if(name.equals("ic2hotcoolant"))return Ic2Fluids.HOT_COOLANT.get();
        if(name.equals("ic2coolant"))return Ic2Fluids.COOLANT.get();
        if(name.equals("ic2pahoehoelava"))return Ic2Fluids.PAHOEHOE_LAVA.get();
        if(name.equals("ic2hotwater"))return Ic2Fluids.HOT_WATER.get();
        if(name.contains(":"))return BuiltInRegistries.FLUID.get(ResourceLocation.parse(name));
        for(var id:BuiltInRegistries.FLUID.keySet())if(id.getPath().equals(name))return BuiltInRegistries.FLUID.get(id);
        return Fluids.EMPTY;
    }
    public static void init(){
        Recipes.FluidHeatGenerator=new ic2.core.FluidHeatManager();
        Recipes.FluidHeatGenerator.addFluid("oil",10,16);Recipes.FluidHeatGenerator.addFluid("fuel",5,64);
        Recipes.FluidHeatGenerator.addFluid("biomass",20,16);Recipes.FluidHeatGenerator.addFluid("bioethanol",10,32);Recipes.FluidHeatGenerator.addFluid("ic2biogas",10,32);
        Recipes.liquidCooldownManager=new ic2.core.LiquidHeatExchangerManager(false);Recipes.liquidHeatupManager=new ic2.core.LiquidHeatExchangerManager(true);
        Recipes.liquidCooldownManager.addFluid("lava","ic2pahoehoelava",20);
        Recipes.liquidCooldownManager.addFluid("ic2hotcoolant","ic2coolant",20);
        Recipes.liquidHeatupManager.addFluid("ic2coolant","ic2hotcoolant",20);
        Recipes.liquidHeatupManager.addFluid("water","ic2hotwater",1);
    }
}
