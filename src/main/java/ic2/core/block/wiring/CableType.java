package ic2.core.block.wiring;

/** Cable kinds of the original blockCable metadata (types 4, 7 and 8 were already migrated away in 2.2.828). */
public enum CableType {
    insulated_copper(0, 6, 0.2, 128, 9001, true),
    copper(1, 4, 0.3, 128, 5, false),
    gold(2, 3, 0.5, 512, 6, false),
    insulated_gold(3, 6, 0.45, 512, 9001, true),
    iron(5, 6, 1.0, 2048, 3, false),
    insulated_iron(6, 10, 0.95, 2048, 9001, true),
    glass_fibre(9, 4, 0.025, 8192, 9001, true),
    tin(10, 4, 0.2, 32, 8, false),
    detector(11, 8, 0.5, 8192, 9001, false),
    splitter(12, 8, 0.5, 8192, 9001, false),
    /** The original gives meta 13 default values (full block thickness, 0.025 loss) by mistake; use insulated tin values instead. */
    insulated_tin(13, 6, 0.2, 32, 9001, true);

    public final int meta;
    public final float thickness;
    public final double loss, insulationAbsorption;
    public final int capacity;
    public final boolean colorable;

    CableType(int meta, int thickness, double loss, int capacity, double insulationAbsorption, boolean colorable) {
        this.meta = meta;
        this.thickness = thickness / 16f;
        this.loss = loss;
        this.capacity = capacity;
        this.insulationAbsorption = insulationAbsorption;
        this.colorable = colorable;
    }

    public String id() { return name() + "_cable"; }

    /** Insulated version (rubber) or null. */
    public CableType insulated() { return switch (this) { case copper -> insulated_copper; case gold -> insulated_gold; case iron -> insulated_iron; case tin -> insulated_tin; default -> null; }; }
    /** Bare version (cutter) or null. */
    public CableType uninsulated() { return switch (this) { case insulated_copper -> copper; case insulated_gold -> gold; case insulated_iron -> iron; case insulated_tin -> tin; default -> null; }; }
}
