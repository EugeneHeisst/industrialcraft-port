package ic2.core.item.armor;
import ic2.core.ref.Ic2Components;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.*;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;
import java.util.*;
import java.util.function.Supplier;
public class ItemArmorFluidTank extends ItemArmorIC2 {
    public final int capacity;private final Supplier<Fluid> fluid;
    public ItemArmorFluidTank(Holder<ArmorMaterial> material,Properties p,Supplier<Fluid> fluid,int capacity){super(material,Type.CHESTPLATE,p.stacksTo(1));this.capacity=capacity;this.fluid=fluid;}
    public FluidHandlerItemStack handler(ItemStack stack){return new FluidHandlerItemStack(Ic2Components.FLUID,stack,capacity){@Override public boolean canFillFluidType(FluidStack f){return f.is(fluid.get());}};}
    public FluidStack getFluid(ItemStack stack){return stack.getOrDefault(Ic2Components.FLUID.get(),SimpleFluidContent.EMPTY).copy();}
    public boolean isEmpty(ItemStack stack){return getFluid(stack).isEmpty();}
    public void filltank(ItemStack stack){handler(stack).fill(new FluidStack(fluid.get(),capacity),net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);}
    public ItemStack filled(){var s=new ItemStack(this);filltank(s);return s;}
    public FluidStack drain(ItemStack stack,int amount,boolean execute){return handler(stack).drain(amount,execute?net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE:net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.SIMULATE);}
    @Override public boolean isBarVisible(ItemStack s){return !isEmpty(s);}
    @Override public int getBarWidth(ItemStack s){return Math.min(13,Math.round(13f*getFluid(s).getAmount()/capacity));}
    @Override public int getBarColor(ItemStack s){return 0x38b8e8;}
    @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> lines,TooltipFlag flag){var f=getFluid(s);lines.add(Component.literal((f.isEmpty()?"":f.getHoverName().getString()+": ")+f.getAmount()+" / "+capacity+" mB"));}
}
