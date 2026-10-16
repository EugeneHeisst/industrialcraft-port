package ic2.core.item.resources;
import ic2.api.item.ILatheItem;
import ic2.core.IC2;
import ic2.core.ref.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import java.util.*;
public class ItemLatheDefault extends Item implements ILatheItem {
    public enum LatheMaterial{WOOD(3,0,0.1f),IRON(5,2,0.5f);final int width,hardness;final float chance;LatheMaterial(int w,int h,float c){width=w;hardness=h;chance=c;}}
    public final LatheMaterial material;
    public ItemLatheDefault(Properties p,LatheMaterial m){super(p.stacksTo(1).component(Ic2Components.LATHE_STATE.get(),List.of(m.width,m.width,m.width,m.width,m.width)));material=m;}
    public int getWidth(ItemStack s){return material.width;}
    public int getHardness(ItemStack s){return material.hardness;}
    public int[] getCurrentState(ItemStack s){var state=s.get(Ic2Components.LATHE_STATE.get());int[] out=new int[5];for(int i=0;i<5;i++)out[i]=state==null||state.size()!=5?material.width:Math.max(1,Math.min(material.width,state.get(i)));return out;}
    public void setState(ItemStack s,int pos,int value){if(pos<0||pos>=5)return;int[] state=getCurrentState(s);state[pos]=Math.max(1,Math.min(material.width,value));s.set(Ic2Components.LATHE_STATE.get(),Arrays.stream(state).boxed().toList());}
    public ItemStack getOutputItem(ItemStack s,int p){return material==LatheMaterial.WOOD?new ItemStack(Items.STICK):new ItemStack(Ic2Items.get("small_iron_dust"));}
    public float getOutputChance(ItemStack s,int p){return material.chance;}
    public ResourceLocation getTexture(ItemStack s){return IC2.id("textures/item/turnables/"+material.name().toLowerCase(Locale.ROOT)+".png");}
    public static int getNumericState(int[] state){if(state==null||state.length!=5)return -1;int n=0;for(int v:state)n=(n<<4)|(v&15);return n;}
    public static int[] getStateFromNumeric(int n){int[] out=new int[5];for(int i=4;i>=0;i--){out[i]=n&15;n>>=4;}return out;}
    public static ItemStack getStackForState(ItemStack s,int[] state){if(state==null||state.length!=5||!(s.getItem() instanceof ILatheItem blank))return ItemStack.EMPTY;var copy=s.copy();for(int i=0;i<5;i++)blank.setState(copy,i,state[i]);return copy;}
    public static ItemStack getStackForState(ItemStack s,int state){return getStackForState(s,getStateFromNumeric(state));}
    @Override public void appendHoverText(ItemStack s,TooltipContext ctx,List<Component> lines,TooltipFlag flag){int[] state=getCurrentState(s);lines.add(Component.literal(Arrays.toString(state)));String key="ic2."+"itemTurningBlanks"+".tooltip."+Integer.toHexString(getNumericState(state));if(net.minecraft.locale.Language.getInstance().has(key))lines.add(Component.translatable(key));}
}
