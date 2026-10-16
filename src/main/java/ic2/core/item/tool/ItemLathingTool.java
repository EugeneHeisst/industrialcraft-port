package ic2.core.item.tool;
import ic2.api.item.*;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.LivingEntity;
public class ItemLathingTool extends Item implements ILatheItem.ILatheTool,IBoxable {
    public ItemLathingTool(Properties p){super(p.durability(50));}
    public int getCustomDamage(ItemStack s){return s.getDamageValue();}public int getMaxCustomDamage(ItemStack s){return s.getMaxDamage();}public void setCustomDamage(ItemStack s,int n){s.setDamageValue(Math.max(0,n));}
    public boolean applyCustomDamage(ItemStack s,int n,LivingEntity e){if(n<=0)return false;s.setDamageValue(s.getDamageValue()+n);if(s.getDamageValue()>=s.getMaxDamage())s.shrink(1);return true;}
    public int getHardness(ItemStack s){return 3;}
    public boolean canBeStoredInToolbox(ItemStack s){return true;}
}
