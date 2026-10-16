package ic2.core.crop;

import com.mojang.serialization.MapCodec;
import ic2.api.crops.*;
import ic2.core.ref.*;
import net.minecraft.core.*;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;
import java.util.Locale;

public class BlockCrop extends BaseEntityBlock {
    public static final MapCodec<BlockCrop> CODEC=simpleCodec(BlockCrop::new);
    public enum Plant implements StringRepresentable {
        EMPTY, WEED, WHEAT, PUMPKIN, MELON, DANDELION, ROSE, BLACKTHORN, TULIP, CYAZINT, VENOMILIA, REED, STICKREED, COCOA, FERRU, AURELIA, REDWHEAT, NETHERWART, TERRAWART, COFFEE, HOPS, CARROTS, POTATO, REDMUSHROOM, BROWNMUSHROOM, EATINGPLANT, CYPRIUM, STAGNIUM, PLUMBISCUS, SHINING;
        public String getSerializedName(){return name().toLowerCase(Locale.ROOT);}
        public static Plant of(String s){try{return valueOf(s.toUpperCase(Locale.ROOT));}catch(IllegalArgumentException e){return EMPTY;}}
    }
    public static final EnumProperty<Plant> PLANT=EnumProperty.create("plant",Plant.class);
    public static final IntegerProperty SIZE=IntegerProperty.create("size",1,7);
    public static final BooleanProperty CROSS=BooleanProperty.create("cross");
    public BlockCrop(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(PLANT,Plant.EMPTY).setValue(SIZE,1).setValue(CROSS,false));}
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(PLANT,SIZE,CROSS);}
    @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Block.box(2,0,2,14,16,14);}
    @Override protected boolean canSurvive(BlockState s,LevelReader l,BlockPos p){return l.getBlockState(p.below()).is(Blocks.FARMLAND);}
    @Override protected BlockState updateShape(BlockState s,Direction d,BlockState n,LevelAccessor l,BlockPos p,BlockPos q){return d==Direction.DOWN&&!canSurvive(s,l,p)?Blocks.AIR.defaultBlockState():s;}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new TileEntityCrop(p,s);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return createTickerHelper(t,Ic2BlockEntities.CROP.get(),(w,p,b,e)->e.tick());}
    @Override protected ItemInteractionResult useItemOn(ItemStack stack,BlockState s,Level l,BlockPos p,Player player,InteractionHand h,BlockHitResult hit){
        if(!(l.getBlockEntity(p) instanceof TileEntityCrop c))return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if(l.isClientSide)return ItemInteractionResult.SUCCESS;
        boolean used=false;
        if(stack.is(this.asItem())&&c.getCrop()==null&&!c.upgraded){c.upgraded=true;c.updateState();used=true;}
        else if(stack.getItem() instanceof ic2.core.item.ItemCropSeed && c.getCrop()==null){
            var n=stack.get(Ic2Components.CROP_DATA.get()); var card=Crops.instance.getCropCard(stack);
            if(n!=null)used=c.tryPlantIn(card,1,n.getByte("growth"),n.getByte("gain"),n.getByte("resistance"),n.getByte("scan"));
        } else if(stack.is(Items.WATER_BUCKET)||stack.is(Ic2Items.WATER_CELL.get())) {
            if(c.waterStorage<10){c.waterStorage=10;c.updateState();return ItemInteractionResult.SUCCESS;}
            if(stack.is(Items.WATER_BUCKET))return ItemInteractionResult.SUCCESS;
        } else if(stack.is(Items.WHEAT_SEEDS)&&c.getCrop()!=null&&c.nutrientStorage<=50){c.nutrientStorage+=25;c.updateState();used=true;}
        else if(stack.is(Ic2Items.get("fertilizer"))||stack.is(Items.BONE_MEAL))used=c.applyFertilizer(true);
        else if(stack.is(Ic2Items.WEED_EX.get())) {if(c.applyWeedEx(true)){stack.hurtAndBreak(1,player,LivingEntity.getSlotForHand(h));return ItemInteractionResult.SUCCESS;}}
        else if(stack.is(Ic2Items.HYDRATION_CELL.get())) {
            int amount=Math.min(200-c.waterStorage,stack.getMaxDamage()-stack.getDamageValue());
            if(amount>0){c.waterStorage+=amount;c.updateState();if(!player.isCreative()){stack.setDamageValue(stack.getDamageValue()+amount);if(stack.getDamageValue()>=stack.getMaxDamage())player.setItemInHand(h,new ItemStack(Ic2Items.EMPTY_CELL.get()));}return ItemInteractionResult.SUCCESS;}
        } else if(stack.is(Ic2Items.CROP_ANALYZER.get()))return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        else if(c.getCrop()==null) {
            var base=Crops.instance.getBaseSeed(stack);
            if(base!=null && stack.getCount()>=base.stackSize && c.tryPlantIn(base.crop,base.size,base.statGrowth,base.statGain,base.statResistance,0)){
                if(!player.isCreative())stack.shrink(base.stackSize);return ItemInteractionResult.SUCCESS;
            }
        }
        if(used){if(!player.isCreative())stack.shrink(1);return ItemInteractionResult.SUCCESS;}
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){
        if(l.isClientSide)return InteractionResult.SUCCESS;
        if(l.getBlockEntity(p) instanceof TileEntityCrop c){
            if(c.upgraded){c.upgraded=false;c.drop(new ItemStack(asItem()));c.updateState();return InteractionResult.SUCCESS;}
            if(c.getCrop()!=null&&c.getCrop().rightclick(c,player))return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
    @Override protected void attack(BlockState s,Level l,BlockPos p,Player player){if(!l.isClientSide&&l.getBlockEntity(p) instanceof TileEntityCrop c&&c.getCrop()!=null)c.getCrop().leftclick(c,player);}
    @Override protected void entityInside(BlockState s,Level l,BlockPos p,Entity e){if(!l.isClientSide&&l.getBlockEntity(p) instanceof TileEntityCrop c&&c.getCrop()!=null&&c.getCrop().onEntityCollision(c,e)&&l.random.nextInt(100)>c.statResistance)c.reset();}
    @Override protected void onRemove(BlockState s,Level l,BlockPos p,BlockState n,boolean moving){
        if(!s.is(n.getBlock())&&!l.isClientSide&&l.getBlockEntity(p) instanceof TileEntityCrop c){if(c.getCrop()!=null){c.getCrop().onBlockDestroyed(c);c.pick(false);}if(c.upgraded)c.drop(new ItemStack(asItem()));}
        super.onRemove(s,l,p,n,moving);
    }
    @Override protected boolean isSignalSource(BlockState s){return true;}
    @Override protected int getSignal(BlockState s,BlockGetter l,BlockPos p,Direction d){return l.getBlockEntity(p) instanceof TileEntityCrop c&&c.getCrop()!=null?c.getCrop().emitRedstone(c):0;}
    @Override public int getLightEmission(BlockState s,BlockGetter l,BlockPos p){return l.getBlockEntity(p) instanceof TileEntityCrop c&&c.getCrop()!=null?c.getCrop().getEmittedLight(c):0;}
}
