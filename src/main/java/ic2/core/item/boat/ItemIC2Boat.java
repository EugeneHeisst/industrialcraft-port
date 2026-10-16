package ic2.core.item.boat;

import java.util.function.Supplier;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.HitResult;

/** Carbon, rubber and electric boat items; the broken rubber boat (kind null) only goes back into a recipe. */
public class ItemIC2Boat extends Item {
    private final Supplier<? extends EntityType<EntityIC2Boat>> type;

    public ItemIC2Boat(Properties p, Supplier<? extends EntityType<EntityIC2Boat>> type) { super(p.stacksTo(1)); this.type = type; }

    /** Places the boat where the player looks, like the vanilla boat item. */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (type == null) return InteractionResultHolder.pass(stack);
        HitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
        if (hit.getType() != HitResult.Type.BLOCK) return InteractionResultHolder.pass(stack);
        var view = player.getViewVector(1);
        for (var e : level.getEntities(player, player.getBoundingBox().expandTowards(view.scale(5)).inflate(1), EntitySelector.NO_SPECTATORS.and(net.minecraft.world.entity.Entity::isPickable)))
            if (e.getBoundingBox().inflate(e.getPickRadius()).contains(player.getEyePosition())) return InteractionResultHolder.pass(stack);
        EntityIC2Boat boat = type.get().create(level);
        boat.moveTo(hit.getLocation().x, hit.getLocation().y, hit.getLocation().z, player.getYRot(), 0);
        if (!level.noCollision(boat, boat.getBoundingBox())) return InteractionResultHolder.fail(stack);
        if (!level.isClientSide) {
            level.addFreshEntity(boat);
            level.gameEvent(player, GameEvent.ENTITY_PLACE, hit.getLocation());
            stack.consume(1, player);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
