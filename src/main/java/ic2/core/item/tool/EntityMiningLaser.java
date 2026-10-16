package ic2.core.item.tool;

import ic2.api.event.LaserEvent;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Entities;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;

/** Mining laser beam: flies 1 block per tick, loses power per block it breaks (explosion resistance / 10) and 0.5 per tick in the air. */
public class EntityMiningLaser extends Entity {
    public float range, power;
    public int blockBreaks;
    public boolean explosive, smelt;
    public LivingEntity owner;
    private int ticksInAir;

    public EntityMiningLaser(EntityType<? extends EntityMiningLaser> type, Level level) { super(type, level); noPhysics = true; }

    public EntityMiningLaser(Level level, LivingEntity owner, float range, float power, int blockBreaks, boolean explosive, double yawDeg, double pitchDeg, double y) {
        this(Ic2Entities.MINING_LASER.get(), level);
        this.owner = owner;
        double yaw = Math.toRadians(yawDeg), pitch = Math.toRadians(pitchDeg);
        setPos(owner.getX() - Math.cos(yaw) * 0.16, y, owner.getZ() - Math.sin(yaw) * 0.16);
        setHeading(-Math.sin(yaw) * Math.cos(pitch), -Math.sin(pitch), Math.cos(yaw) * Math.cos(pitch));
        this.range = range; this.power = power; this.blockBreaks = blockBreaks; this.explosive = explosive;
    }

    public void setHeading(double x, double y, double z) {
        double len = Math.sqrt(x * x + y * y + z * z);
        setDeltaMovement(x / len, y / len, z / len);
        setYRot((float)Math.toDegrees(Math.atan2(x, z)));
        setXRot((float)Math.toDegrees(Math.atan2(y, Math.sqrt(x * x + z * z))));
        yRotO = getYRot(); xRotO = getXRot();
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {}
    @Override protected void readAdditionalSaveData(CompoundTag tag) { discard(); }
    @Override protected void addAdditionalSaveData(CompoundTag tag) {}
    @Override public boolean shouldRenderAtSqrDistance(double d) { return d < 160 * 160; }

    public boolean takeDataFromEvent(LaserEvent e) {
        owner = e.owner; range = e.range; power = e.power; blockBreaks = e.blockBreaks; explosive = e.explosive; smelt = e.smelt;
        if (e.isCanceled()) { discard(); return false; }
        return true;
    }

    public static boolean canMine(BlockState s) {
        return !(s.is(Blocks.BRICKS) || s.is(Blocks.OBSIDIAN) || s.is(Blocks.BEDROCK) || s.is(Blocks.LAVA) || s.is(Blocks.WATER)
            || s.is(Ic2Blocks.REINFORCED_STONE.get()) || s.is(Ic2Blocks.REINFORCED_DOOR.get()));
    }

    @Override
    public void tick() {
        super.tick();
        boolean server = !level().isClientSide;
        if (server && (range < 1 || power <= 0 || blockBreaks <= 0)) { if (explosive) explode(); discard(); return; }
        ticksInAir++;
        Vec3 m = getDeltaMovement();
        Vec3 from = position(), to = from.add(m);
        HitResult hit = level().clip(new ClipContext(from, to, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this));
        if (hit.getType() != HitResult.Type.MISS) to = hit.getLocation();
        Entity hitEntity = null;
        double best = 0;
        for (Entity e : level().getEntities(this, getBoundingBox().expandTowards(m).inflate(1))) {
            if (!e.isPickable() || e == owner && ticksInAir < 5) continue;
            var clip = e.getBoundingBox().inflate(0.3).clip(from, to);
            if (clip.isPresent()) { double d = from.distanceTo(clip.get()); if (d < best || best == 0) { hitEntity = e; best = d; } }
        }
        if (hitEntity != null) hit = new EntityHitResult(hitEntity);
        if (hit.getType() != HitResult.Type.MISS && server) {
            if (explosive) { explode(); discard(); return; }
            if (hit instanceof EntityHitResult eh) hitEntity(eh.getEntity());
            else if (hit instanceof BlockHitResult bh) hitBlock(bh);
        } else power -= 0.5f;
        setPos(getX() + m.x, getY() + m.y, getZ() + m.z);
        range -= (float)m.length();
        if (isInWater()) discard();
    }

    private void hitEntity(Entity target) {
        var e = new LaserEvent.LaserHitsEntityEvent(level(), this, owner, range, power, blockBreaks, explosive, smelt, target);
        NeoForge.EVENT_BUS.post(e);
        if (!takeDataFromEvent(e)) return;
        int damage = (int)power;
        if (damage > 0) {
            target.igniteForSeconds(damage * (smelt ? 2 : 1));
            DamageSource src = owner == null ? damageSources().magic() : damageSources().indirectMagic(this, owner);
            e.hitEntity.hurt(src, damage);
        }
        discard();
    }

    private void hitBlock(BlockHitResult bh) {
        var e = new LaserEvent.LaserHitsBlockEvent(level(), this, owner, range, power, blockBreaks, explosive, smelt, bh.getBlockPos(), bh.getDirection(), 0.9f, true, true);
        NeoForge.EVENT_BUS.post(e);
        if (!takeDataFromEvent(e)) return;
        Level level = level();
        BlockPos pos = e.pos;
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.is(BlockTags.IMPERMEABLE) || state.is(Blocks.GLASS_PANE) || state.is(Ic2Blocks.REINFORCED_GLASS.get())) return;
        if (!canMine(state)) { discard(); return; }
        power -= (state.getExplosionResistance(level, pos, null) + 0.3f) / 10f;
        if (power < 0) return;
        if (state.getBlock() instanceof TntBlock) { state.onCaughtFire(level, pos, null, owner); level.removeBlock(pos, false); blockBreaks--; return; }
        if (smelt) {
            if (state.is(BlockTags.LOGS) || state.is(BlockTags.PLANKS)) e.dropBlock = false;
            else for (ItemStack drop : Block.getDrops(state, (ServerLevel)level, pos, level.getBlockEntity(pos))) {
                var recipe = level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(drop), level);
                if (recipe.isEmpty()) continue;
                ItemStack result = recipe.get().value().getResultItem(level.registryAccess()).copy();
                Block newBlock = Block.byItem(result.getItem());
                e.dropBlock = false;
                if (newBlock != Blocks.AIR && newBlock != state.getBlock()) { e.removeBlock = false; level.setBlock(pos, newBlock.defaultBlockState(), 3); }
                else {
                    var item = new ItemEntity(level, pos.getX() + 0.15 + random.nextFloat() * 0.7, pos.getY() + 0.15 + random.nextFloat() * 0.7, pos.getZ() + 0.15 + random.nextFloat() * 0.7, result);
                    item.setPickUpDelay(10);
                    level.addFreshEntity(item);
                }
                power = 0;
            }
        }
        if (e.removeBlock) {
            if (e.dropBlock && random.nextFloat() <= e.dropChance) Block.dropResources(state, level, pos, level.getBlockEntity(pos), owner, ItemStack.EMPTY);
            level.removeBlock(pos, false);
            if (random.nextInt(10) == 0 && state.ignitedByLava() && BaseFireBlock.canBePlacedAt(level, pos, bh.getDirection())) level.setBlock(pos, BaseFireBlock.getState(level, pos), 11);
        }
        blockBreaks--;
    }

    public void explode() {
        var e = new LaserEvent.LaserExplodesEvent(level(), this, owner, range, power, blockBreaks, explosive, smelt, 5, 0.85f, 0.55f);
        NeoForge.EVENT_BUS.post(e);
        if (takeDataFromEvent(e)) ic2.core.ExplosionIC2.explode(level(), this, owner, getX(), getY(), getZ(), e.explosionPower, e.explosionDropRate);
    }
}
