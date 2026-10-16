package ic2.core.block.machine.tileentity;

import ic2.api.item.ITerraformingBP;
import ic2.core.audio.AudioSource;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumable;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Sounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Terraformer: applies the inserted blueprint at random spots around it (right click with a blueprint inserts, empty hand ejects). */
public class TileEntityTerra extends TileEntityElectricMachine {
    public int failedAttempts, lastX = -1, lastY = -1, lastZ = -1, inactiveTicks;
    private AudioSource audioSource;
    public final InvSlotConsumable tfbpSlot = new InvSlotConsumable(this, "tfbp", InvSlot.Access.I, 1, InvSlot.InvSide.ANY) {
        @Override public boolean accepts(ItemStack s) { return s.getItem() instanceof ITerraformingBP; }
    };

    public TileEntityTerra(BlockPos pos, BlockState state) { super(Ic2BlockEntities.TERRAFORMER.get(), pos, state, 100000, 3); }

    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        boolean active = false;
        if (!tfbpSlot.isEmpty() && tfbpSlot.get().getItem() instanceof ITerraformingBP tfbp && energy >= tfbp.getConsume()) {
            active = true;
            var r = level.random;
            int x, z, range;
            if (lastY > -1) {
                range = tfbp.getRange() / 10;
                x = lastX - r.nextInt(range + 1) + r.nextInt(range + 1);
                z = lastZ - r.nextInt(range + 1) + r.nextInt(range + 1);
            } else {
                failedAttempts = Math.min(failedAttempts, 4);
                range = tfbp.getRange() * (failedAttempts + 1) / 5;
                x = worldPosition.getX() - r.nextInt(range + 1) + r.nextInt(range + 1);
                z = worldPosition.getZ() - r.nextInt(range + 1) + r.nextInt(range + 1);
            }
            if (level.isLoaded(new BlockPos(x, worldPosition.getY(), z)) && tfbp.terraform(level, x, z, worldPosition.getY())) {
                energy -= tfbp.getConsume();
                failedAttempts = 0;
                lastX = x; lastZ = z; lastY = worldPosition.getY();
            } else {
                energy -= tfbp.getConsume() / 10.0;
                failedAttempts++;
                lastY = -1;
            }
        }
        if (active) { inactiveTicks = 0; setActive(true); }
        else if (getActive() && inactiveTicks++ > 30) setActive(false);
    }

    @Override
    public boolean onActivated(Player player) {
        if (player.isShiftKeyDown()) return false;
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) { if (!level.isClientSide) ejectBlueprint(); return true; }
        if (!(held.getItem() instanceof ITerraformingBP)) return false;
        if (!level.isClientSide) { ejectBlueprint(); tfbpSlot.put(held.split(1)); setChanged(); }
        return true;
    }

    public boolean ejectBlueprint() {
        if (tfbpSlot.isEmpty()) return false;
        Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 1, worldPosition.getZ() + 0.5, tfbpSlot.get());
        tfbpSlot.put(ItemStack.EMPTY);
        return true;
    }

    @Override public double injectEnergy(Direction from, double amount, double voltage) { if (energy >= maxEnergy) return amount; energy += amount; return 0; }

    private boolean lastClientActive;
    @Override protected void updateEntityClient() {
        boolean a = getActive();
        if (a == lastClientActive) return;
        lastClientActive = a;
        if (audioSource == null) audioSource = AudioSource.create(this, Ic2Sounds.TERRAFORMER_LOOP.get());
        if (audioSource != null) { if (a) audioSource.play(); else audioSource.stop(); }
    }
    @Override public void onUnloaded() { if (audioSource != null) { audioSource.remove(); audioSource = null; } super.onUnloaded(); }
}
