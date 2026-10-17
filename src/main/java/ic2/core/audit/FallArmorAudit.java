package ic2.core.audit;

import com.mojang.authlib.GameProfile;
import ic2.api.item.ElectricItem;
import ic2.core.ref.Ic2Items;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

/** Real native fall damage, with expected values from original Nano/Quantum ISpecialArmor properties. */
public final class FallArmorAudit {
    private record Scenario(String name,Item boots,double charge,float distance,float multiplier,
                            float health,double remaining,double safeFallDistance) {}

    private static ServerPlayer player(ServerLevel level,BlockPos pos) throws ReflectiveOperationException {
        var player=new ServerPlayer(level.getServer(),level,new GameProfile(UUID.randomUUID(),"IC2FallAudit"),ClientInformation.createDefault());
        player.connection=FakePlayerFactory.getMinecraft(level).connection;
        player.setGameMode(GameType.SURVIVAL);
        player.getAbilities().invulnerable=false;
        var spawn=ServerPlayer.class.getDeclaredField("spawnInvulnerableTime");
        spawn.setAccessible(true);
        spawn.setInt(player,0);
        player.setPos(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5);
        player.setHealth(20);
        player.invulnerableTime=0;
        return player;
    }

    private static ItemStack boots(Item item,double charge) {
        if(item==null)return ItemStack.EMPTY;
        var boots=new ItemStack(item);
        ElectricItem.manager.charge(boots,charge,Integer.MAX_VALUE,true,false);
        return boots;
    }

    public static void run(ServerLevel level,BlockPos pos,CropAudit.Check check) {
        level.getChunkAt(pos);
        var nano=Ic2Items.NANO_BOOTS.get();
        var quantum=Ic2Items.QUANTUM_BOOTS.get();
        check.test("fall armor fixture uses native armor-bypassing fall source",level.damageSources().fall().is(DamageTypeTags.BYPASSES_ARMOR),"minecraft:fall");
        var scenarios=List.of(
            new Scenario("bare twenty-block fall",null,0,20,1,3,0,3),
            new Scenario("nano long fall original87.5percent absorption",nano,1000000,20,1,17.875f,930000,3),
            new Scenario("nano short fall retains full cancellation",nano,1000000,9,1,20,970000,3),
            new Scenario("nano short fall partial charge still absorbs",nano,10000,9,1,16,0,3),
            new Scenario("nano long fall partial charge still absorbs",nano,10000,20,1,5,0,3),
            new Scenario("empty nano cannot reduce long fall",nano,0,20,1,3,0,3),
            new Scenario("nano fractional budget preserves original25scale",nano,2500,20,1,3.48f,0,3),
            new Scenario("nano below original absorption quantum spends nothing",nano,199,20,1,3,199,3),
            new Scenario("quantum long fall retains full cancellation",quantum,10000000,20,1,20,9800000,3),
            new Scenario("quantum partial charge still absorbs",quantum,20000,20,1,4,0,3),
            new Scenario("empty quantum cannot reduce long fall",quantum,0,20,1,3,0,3),
            new Scenario("empty quantum retains original short-fall guard",quantum,0,9,1,20,0,3),
            new Scenario("quantum fractional budget preserves original25scale",quantum,10000,20,1,3.48f,0,3),
            new Scenario("nano absorption uses actual native damage multiplier",nano,1000000,20,.5f,18.875f,965000,3),
            new Scenario("nano absorption uses actual native safe-fall attribute",nano,1000000,20,1,18.125f,935000,5)
        );
        try {
            for(var scenario:scenarios) {
                var player=player(level,pos);
                var boots=boots(scenario.boots,scenario.charge);
                player.setItemSlot(EquipmentSlot.FEET,boots);
                player.getAttribute(Attributes.SAFE_FALL_DISTANCE).setBaseValue(scenario.safeFallDistance);
                player.causeFallDamage(scenario.distance,scenario.multiplier,player.damageSources().fall());
                double remaining=boots.isEmpty()?0:ElectricItem.manager.getCharge(boots);
                check.test("fall armor "+scenario.name,Math.abs(player.getHealth()-scenario.health)<.0001&&Math.abs(remaining-scenario.remaining)<.0001,
                    "health="+player.getHealth()+" expected="+scenario.health+" EU="+remaining+" expected="+scenario.remaining);
                player.setItemSlot(EquipmentSlot.FEET,ItemStack.EMPTY);
            }
            var player=player(level,pos);
            var boots=boots(nano,1000000);
            player.setItemSlot(EquipmentSlot.FEET,boots);
            player.hurt(player.damageSources().magic(),4);
            check.test("fall armor fallback does not protect unrelated armor-bypassing magic",player.getHealth()==16&&ElectricItem.manager.getCharge(boots)==1000000,"health="+player.getHealth()+" EU="+ElectricItem.manager.getCharge(boots));
            player.setHealth(20);player.invulnerableTime=0;
            player.hurt(player.damageSources().cactus(),10);
            check.test("fall armor fallback keeps ordinary melee absorption and current payment",Math.abs(player.getHealth()-11.35)<.0001&&ElectricItem.manager.getCharge(boots)==995000,"health="+player.getHealth()+" EU="+ElectricItem.manager.getCharge(boots));
            player.setItemSlot(EquipmentSlot.FEET,ItemStack.EMPTY);
        } catch(ReflectiveOperationException failure) {
            throw new RuntimeException("IC2 fall armor audit fixture",failure);
        }
    }
}
