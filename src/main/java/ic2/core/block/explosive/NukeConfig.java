package ic2.core.block.explosive;
import net.neoforged.neoforge.common.ModConfigSpec;
public final class NukeConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ENABLE_NUKE;
    public static final ModConfigSpec.DoubleValue POWER_LIMIT;
    static {var b=new ModConfigSpec.Builder();b.push("protection");ENABLE_NUKE=b.comment("Allow nuclear bomb ignition.").define("enableNuke",true);POWER_LIMIT=b.comment("Maximum nuclear bomb explosion power (original IC2 default: 60).").defineInRange("nukeExplosionPowerLimit",60d,0d,1000d);b.pop();SPEC=b.build();}
    private NukeConfig(){}
}
