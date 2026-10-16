package ic2.api.tile;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.world.level.block.Block;

/** Blocks with huge blast resistance that IC2 explosions may still destroy. */
public final class ExplosionWhitelist {
    private static final Set<Block> whitelist = new HashSet<>();
    private ExplosionWhitelist() {}
    public static void addWhitelistedBlock(Block block) { whitelist.add(block); }
    public static void removeWhitelistedBlock(Block block) { whitelist.remove(block); }
    public static boolean isBlockWhitelisted(Block block) { return whitelist.contains(block); }
}
