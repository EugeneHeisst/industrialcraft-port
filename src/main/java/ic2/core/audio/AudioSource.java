package ic2.core.audio;

import java.util.function.BiFunction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Looping machine sound of the original audio manager; the client installs the real implementation. */
public abstract class AudioSource {
    public static BiFunction<BlockEntity, SoundEvent, AudioSource> factory = (be, sound) -> null;

    public static AudioSource create(BlockEntity be, SoundEvent sound) { return factory.apply(be, sound); }

    public static void playOnce(BlockEntity be, SoundEvent sound) {
        var level = be.getLevel();
        if (level != null && level.isClientSide) {
            var pos = be.getBlockPos();
            level.playLocalSound(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, sound, SoundSource.BLOCKS, 1, 1, false);
        }
    }

    public abstract void play();
    public abstract void stop();
    public abstract void remove();
    public abstract boolean isPlaying();
}
