package ic2.core.client;

import ic2.core.audio.AudioSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Looping machine sound bound to a block entity. */
public class MachineSound extends AudioSource {
    private final BlockEntity be;
    private final SoundEvent sound;
    private Instance instance;

    public MachineSound(BlockEntity be, SoundEvent sound) { this.be = be; this.sound = sound; }

    @Override public void play() {
        if (isPlaying()) return;
        instance = new Instance();
        Minecraft.getInstance().getSoundManager().play(instance);
    }
    @Override public void stop() { if (instance != null) { instance.end(); instance = null; } }
    @Override public void remove() { stop(); }
    @Override public boolean isPlaying() { return instance != null && !instance.isStopped(); }

    private class Instance extends AbstractTickableSoundInstance {
        Instance() {
            super(MachineSound.this.sound, SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
            looping = true;
            delay = 0;
            volume = 1;
            x = be.getBlockPos().getX() + .5;
            y = be.getBlockPos().getY() + .5;
            z = be.getBlockPos().getZ() + .5;
        }
        void end() { stop(); }
        @Override public void tick() { if (be.isRemoved()) stop(); }
    }
}
