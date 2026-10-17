package ic2.core.item.tool;

import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.core.item.BaseElectricItem;
import ic2.core.item.ElectricItemManager;
import java.util.List;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

/** Energy powered tool: digs at its efficiency while it can pay operationEnergyCost per broken block. */
public abstract class ItemElectricTool extends Item implements IElectricItem {
    public enum HarvestLevel {
        Wood(0), Stone(1), Iron(2), Diamond(3), Iridium(100);
        public final int level;
        HarvestLevel(int level) { this.level = level; }
        boolean canHarvest(BlockState state) {
            if (state.is(BlockTags.NEEDS_DIAMOND_TOOL)) return level >= 3;
            if (state.is(BlockTags.NEEDS_IRON_TOOL)) return level >= 2;
            if (state.is(BlockTags.NEEDS_STONE_TOOL)) return level >= 1;
            return true;
        }
    }

    public enum ToolClass {
        Axe(BlockTags.MINEABLE_WITH_AXE, ItemAbilities.DEFAULT_AXE_ACTIONS), Pickaxe(BlockTags.MINEABLE_WITH_PICKAXE, ItemAbilities.DEFAULT_PICKAXE_ACTIONS),
        Shears(BlockTags.LEAVES, ItemAbilities.DEFAULT_SHEARS_ACTIONS), Shovel(BlockTags.MINEABLE_WITH_SHOVEL, ItemAbilities.DEFAULT_SHOVEL_ACTIONS),
        Sword(BlockTags.SWORD_EFFICIENT, ItemAbilities.DEFAULT_SWORD_ACTIONS), Hoe(BlockTags.MINEABLE_WITH_HOE, ItemAbilities.DEFAULT_HOE_ACTIONS);
        public final TagKey<Block> mineable;
        public final Set<ItemAbility> abilities;
        ToolClass(TagKey<Block> mineable, Set<ItemAbility> abilities) { this.mineable = mineable; this.abilities = abilities; }
    }

    public double operationEnergyCost;
    public int maxCharge, transferLimit, tier;
    public float efficiencyOnProperMaterial = 4;
    protected final HarvestLevel harvestLevel;
    protected final Set<ToolClass> toolClasses;

    protected ItemElectricTool(Properties properties, int operationEnergyCost, HarvestLevel harvestLevel, Set<ToolClass> toolClasses) {
        super(properties.stacksTo(1).setNoRepair());
        this.operationEnergyCost = operationEnergyCost;
        this.harvestLevel = harvestLevel;
        this.toolClasses = toolClasses;
    }

    protected boolean isEffectiveOn(BlockState state) {
        for (ToolClass c : toolClasses) {
            if (state.is(c.mineable)) return true;
            if (c == ToolClass.Shears && (state.is(Blocks.COBWEB) || state.is(BlockTags.WOOL) || state.is(Blocks.REDSTONE_WIRE) || state.is(Blocks.TRIPWIRE))) return true;
            if (c == ToolClass.Sword && state.is(Blocks.COBWEB)) return true;
        }
        return false;
    }

    /** EU per operation for this stack; addons with per-stack modes (Gravi advanced drill) override it, the TC bore reads it. */
    public double getEnergyCost(ItemStack stack) { return operationEnergyCost; }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (!ElectricItem.manager.canUse(stack, getEnergyCost(stack))) return 1;
        return isEffectiveOn(state) ? efficiencyOnProperMaterial : 1;
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return isEffectiveOn(state) && harvestLevel.canHarvest(state);
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility ability) {
        for (ToolClass c : toolClasses) if (c.abilities.contains(ability)) return true;
        return false;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        if (state.getDestroySpeed(level, pos) != 0) {
            if (entity != null) ElectricItem.manager.use(stack, getEnergyCost(stack), entity);
            else ElectricItem.manager.discharge(stack, getEnergyCost(stack), tier, true, false, false);
        }
        return true;
    }

    /**
     * Original onItemRightClick / onItemUse: ElectricItem.manager.use(stack, 0, player) tops the tool up from charging armour
     * (batpack, lappack) without spending EU. Overrides that kept super in the original return super.use(...).
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) ElectricItem.manager.use(stack, 0, player);
        return InteractionResultHolder.pass(stack);
    }

    /** Called by destroyBlock after the break was approved, just before the tool is copied for the drops (original onBlockStartBreak). */
    public void beforeHarvestSnapshot(ItemStack stack, net.minecraft.server.level.ServerLevel level, BlockPos pos, BlockState state, net.minecraft.server.level.ServerPlayer player) {}

    @Override public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) { return true; }
    @Override public int getEnchantmentValue(ItemStack stack) { return 0; }
    @Override public boolean isEnchantable(ItemStack stack) { return false; }
    @Override public boolean isBookEnchantable(ItemStack stack, ItemStack book) { return false; }
    @Override public boolean canProvideEnergy(ItemStack stack) { return false; }
    @Override public double getMaxCharge(ItemStack stack) { return maxCharge; }
    @Override public int getTier(ItemStack stack) { return tier; }
    @Override public double getTransferLimit(ItemStack stack) { return transferLimit; }

    @Override public boolean isBarVisible(ItemStack stack) { return true; }
    @Override public int getBarWidth(ItemStack stack) { return Math.round(13f * (float)Mth.clamp(ElectricItemManager.getRawCharge(stack) / maxCharge, 0, 1)); }
    @Override public int getBarColor(ItemStack stack) { return Mth.hsvToRgb((float)Mth.clamp(ElectricItemManager.getRawCharge(stack) / maxCharge, 0, 1) / 3f, 1, 1); }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> list, TooltipFlag flag) {
        String tip = ElectricItem.manager.getToolTip(stack);
        if (tip != null) list.add(Component.literal(tip).withStyle(ChatFormatting.GRAY));
        list.add(Component.literal(Component.translatable("ic2.item.tooltip.PowerTier").getString() + " " + tier).withStyle(ChatFormatting.GRAY));
    }

    public ItemStack getItemStack(double charge) {
        ItemStack ret = new ItemStack(this);
        ElectricItem.manager.charge(ret, charge, Integer.MAX_VALUE, true, false);
        return ret;
    }
}
