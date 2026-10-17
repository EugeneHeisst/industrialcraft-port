package ic2.core.audit;

import com.mojang.authlib.GameProfile;
import ic2.api.item.ElectricItem;
import ic2.core.ref.Ic2Items;
import java.util.Arrays;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

/**
 * Ordinary (non-fall) electric armour hits on a real survival ServerPlayer, expected values from the original IC2 ISpecialArmor
 * properties run through Forge 1.7.10 ArmorProperties.ApplyArmor/StandardizeList: a priority's ratios above 1 are normalised,
 * each piece pays floor(absorbed) (at least 1) x energyPerDamage, absorption is capped by charge on the 25-scale.
 */
public final class ArmorMathAudit {
    private ArmorMathAudit() {}

    private static ServerPlayer player(ServerLevel level, BlockPos pos) throws ReflectiveOperationException {
        var player = new ServerPlayer(level.getServer(), level, new GameProfile(UUID.randomUUID(), "IC2ArmorAudit"), ClientInformation.createDefault());
        player.connection = FakePlayerFactory.getMinecraft(level).connection;
        player.setGameMode(GameType.SURVIVAL);
        player.getAbilities().invulnerable = false;
        var spawn = ServerPlayer.class.getDeclaredField("spawnInvulnerableTime");
        spawn.setAccessible(true);
        spawn.setInt(player, 0);
        player.setPos(pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5);
        player.setHealth(20);
        player.invulnerableTime = 0;
        return player;
    }

    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    /** pieces in head/chest/legs/feet order (null = empty); returns the EU each piece spent. */
    private static double[] hit(ServerLevel level, BlockPos pos, Item[] pieces, double charge, float damage, float health, CropAudit.Check check, String name) throws ReflectiveOperationException {
        var p = player(level, pos);
        double[] before = new double[4];
        for (int i = 0; i < 4; i++) {
            if (pieces[i] == null) continue;
            var s = new ItemStack(pieces[i]);
            ElectricItem.manager.charge(s, charge, Integer.MAX_VALUE, true, false);
            before[i] = ElectricItem.manager.getCharge(s);
            p.setItemSlot(SLOTS[i], s);
        }
        p.hurt(level.damageSources().cactus(), damage);
        double[] spent = new double[4];
        for (int i = 0; i < 4; i++) spent[i] = pieces[i] == null ? 0 : before[i] - ElectricItem.manager.getCharge(p.getItemBySlot(SLOTS[i]));
        check.test(name + ": health", Math.abs(p.getHealth() - health) < 1e-4, p.getHealth());
        p.discard();
        return spent;
    }

    public static void run(ServerLevel level, BlockPos pos, CropAudit.Check check) {
        level.getChunkAt(pos);
        try {
            var nh = Ic2Items.NANO_HELMET.get();
            Item[] nanoHelmet = {nh, null, null, null};
            // .15 x .9 = .135 of 12 = 1.62 absorbed, floor -> 1 damage point -> 5000 EU (the port rounded to 2 -> 10000)
            var a = hit(level, pos, nanoHelmet, 1e9, 12, 20 - (12 - 1.62f), check, "nano helmet alone, 12 damage");
            check.test("nano helmet alone pays floor(1.62)=1 x 5000 EU", a[0] == 5000, Arrays.toString(a));
            // only 2500 EU: 25-scale budget int(25*2500/5000)=12 -> absorbs 12/25 = .48, still pays the minimum one point (drains it)
            var c = hit(level, pos, nanoHelmet, 2500, 12, 20 - (12 - .48f), check, "nano helmet with 2500 EU, 12 damage");
            check.test("charge-capped absorption drains the helmet", c[0] == 2500, Arrays.toString(c));
            Item[] quantum = {Ic2Items.QUANTUM_HELMET.get(), Ic2Items.QUANTUM_CHESTPLATE.get(), Ic2Items.QUANTUM_LEGGINGS.get(), Ic2Items.QUANTUM_BOOTS.get()};
            // ratios .15/.44/.30/.15 = 1.04 > 1 -> normalised; 10 damage -> 1.44/4.23/2.88/1.44 -> 1/4/2/1 x 20000
            var b = hit(level, pos, quantum, 1e9, 10, 20, check, "full quantum, 10 damage");
            check.test("full quantum pays 20000/80000/40000/20000 = 160000 EU (port: 220000)", b[0] == 20000 && b[1] == 80000 && b[2] == 40000 && b[3] == 20000, Arrays.toString(b));
            Item[] nano = {nh, Ic2Items.NANO_CHESTPLATE.get(), Ic2Items.NANO_LEGGINGS.get(), Ic2Items.NANO_BOOTS.get()};
            // ratios .135/.36/.27/.135 = .9, no normalisation; 10 damage -> 1.35/3.6/2.7/1.35 -> 1/3/2/1 x 5000, 1 damage gets through
            var d = hit(level, pos, nano, 1e9, 10, 19, check, "full nano, 10 damage");
            check.test("full nano pays 5000/15000/10000/5000 EU", d[0] == 5000 && d[1] == 15000 && d[2] == 10000 && d[3] == 5000, Arrays.toString(d));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
