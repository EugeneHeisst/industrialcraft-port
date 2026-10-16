package ic2.core.item;
import ic2.api.crops.Crops;
import ic2.core.crop.TileEntityCrop;
import ic2.core.ref.Ic2Items;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
public class ItemWeedingTrowel extends Item {
    public ItemWeedingTrowel(Properties p){super(p.stacksTo(1));}
    @Override public InteractionResult useOn(UseOnContext c){
        if(c.getLevel().getBlockEntity(c.getClickedPos()) instanceof TileEntityCrop crop && crop.getCrop()==Crops.weed){
            if(!c.getLevel().isClientSide){crop.drop(new ItemStack(Ic2Items.get("weed"),crop.size));crop.reset();}
            return InteractionResult.sidedSuccess(c.getLevel().isClientSide);
        }
        return InteractionResult.PASS;
    }
}
