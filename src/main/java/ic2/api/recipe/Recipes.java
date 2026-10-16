package ic2.api.recipe;

public final class Recipes {
    public static IMachineRecipeManager macerator, extractor, compressor, centrifuge, blockcutter, blastfurance, recycler,
        metalformerExtruding, metalformerCutting, metalformerRolling, oreWashing, matterAmplifier;
    public static IFluidHeatManager FluidHeatGenerator;
    public static ILiquidHeatExchangerManager liquidCooldownManager, liquidHeatupManager;
    public static IScrapboxManager scrapboxDrops;
    public static ICannerBottleRecipeManager cannerBottle;
    public static ICannerEnrichRecipeManager cannerEnrich;
    public static IListRecipeManager recyclerBlacklist, recyclerWhitelist;
    private Recipes() {}
}
