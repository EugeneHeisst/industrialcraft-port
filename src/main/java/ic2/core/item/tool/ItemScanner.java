package ic2.core.item.tool;

import ic2.api.item.ElectricItem;
import ic2.core.IC2;
import ic2.core.item.BaseElectricItem;
import ic2.core.util.OreValues;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** OD scanner (range 6, 50 EU) and OV scanner (range 12, 250 EU): lists ore drops around the player; also feeds the miner. */
public class ItemScanner extends BaseElectricItem {
    private final int range, cost;

    public ItemScanner(Properties properties, double maxCharge, double transferLimit, int tier, int range, int cost) {
        super(properties.stacksTo(1), maxCharge, transferLimit, tier);
        this.range = range;
        this.cost = cost;
    }

    public int getScannrange() { return range; }

    /** Miner layer scan: pays the scan cost and returns the radius (half the hand range). */
    public int startLayerScan(ItemStack stack) { return ElectricItem.manager.use(stack, cost, null) ? range / 2 : 0; }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!ElectricItem.manager.use(stack, cost, player)) return InteractionResultHolder.pass(stack);
        if (player instanceof ServerPlayer sp) {
            Map<Item, Integer> found = new LinkedHashMap<>();
            BlockPos center = player.blockPosition();
            for (BlockPos p : BlockPos.betweenClosed(center.offset(-range, -range, -range), center.offset(range, range, range))) {
                var state = level.getBlockState(p);
                if (!OreValues.isValuable(state)) continue;
                for (ItemStack drop : Block.getDrops(state, (ServerLevel)level, p, null)) found.merge(drop.getItem(), drop.getCount(), Integer::sum);
            }
            List<ItemStack> list = new ArrayList<>();
            found.entrySet().stream().sorted((a, b) -> b.getValue() - a.getValue()).forEach(e -> list.add(new ItemStack(e.getKey(), Math.min(99, e.getValue()))));
            List<Integer> counts = new ArrayList<>();
            found.entrySet().stream().sorted((a, b) -> b.getValue() - a.getValue()).forEach(e -> counts.add(e.getValue()));
            PacketDistributor.sendToPlayer(sp, new ScanResult(list, counts));
        } else level.playLocalSound(player.getX(), player.getY(), player.getZ(), ic2.core.ref.Ic2Sounds.OD_SCANNER.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1, 1, false);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    public record ScanResult(List<ItemStack> items, List<Integer> counts) implements CustomPacketPayload {
        public static final Type<ScanResult> TYPE = new Type<>(IC2.id("scan_result"));
        public static final StreamCodec<RegistryFriendlyByteBuf, ScanResult> CODEC = StreamCodec.composite(ItemStack.LIST_STREAM_CODEC, ScanResult::items, ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()), ScanResult::counts, ScanResult::new);
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
        public static java.util.function.BiConsumer<ScanResult, IPayloadContext> clientHandler = (p, c) -> {};
        public static void handle(ScanResult p, IPayloadContext ctx) { clientHandler.accept(p, ctx); }
    }
}
