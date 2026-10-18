package ic2.core.audit;

import com.mojang.blaze3d.platform.NativeImage;
import ic2.core.IC2;
import ic2.core.block.misc.BlockScaffold;
import ic2.core.ref.Ic2Blocks;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Audit-only gallery: actual GUI items and actual normal/reinforced scaffold block models. */
public final class VisualGalleryScreen extends Screen {
    private static final int ICON_SIZE = 32;
    private static final int BACKGROUND = 0xff273b4d;
    private static VisualGalleryScreen active;
    private final List<Entry> machines = List.of(
        item("Iron furn.", Ic2Blocks.IRON_FURNACE.get()),
        item("E. furnace", Ic2Blocks.ELECTRIC_FURNACE.get()),
        item("Macerator", Ic2Blocks.MACERATOR.get()),
        item("Extractor", Ic2Blocks.EXTRACTOR.get()),
        item("Compressor", Ic2Blocks.COMPRESSOR.get()),
        item("Recycler", Ic2Blocks.RECYCLER.get()),
        item("Generator", Ic2Blocks.GENERATOR.get()),
        item("Geothermal", Ic2Blocks.GEOTHERMAL_GENERATOR.get()),
        item("Induction", Ic2Blocks.INDUCTION_FURNACE.get()),
        item("Metal form", Ic2Blocks.METAL_FORMER.get()),
        item("Ore wash", Ic2Blocks.ORE_WASHING_PLANT.get()),
        item("Centrifuge", Ic2Blocks.THERMAL_CENTRIFUGE.get()),
        item("Canner", Ic2Blocks.CANNER.get()),
        item("Miner", Ic2Blocks.MINER.get()),
        item("Pump", Ic2Blocks.PUMP.get()),
        item("Mass fab", Ic2Blocks.MASS_FABRICATOR.get()),
        item("MFSU", Ic2Blocks.MFSU.get()),
        item("LV xfmr", Ic2Blocks.LV_TRANSFORMER.get()),
        item("Scanner", Ic2Blocks.SCANNER.get()),
        item("Replicator", Ic2Blocks.REPLICATOR.get()),
        new Entry("Mining pipe", new ItemStack(ic2.core.ref.Ic2Items.get("mining_pipe")), null),
        new Entry("Pulling upgrade", new ItemStack(ic2.core.ref.Ic2Items.get("pulling_upgrade")), null),
        item("Stirling kU", Ic2Blocks.STIRLING_KINETIC_GENERATOR.get())
    );
    private final List<Entry> scaffolds = List.of(
        block("Wood", Ic2Blocks.SCAFFOLD.get().defaultBlockState().setValue(BlockScaffold.STRENGTH, 2)),
        block("Wood braced", Ic2Blocks.SCAFFOLD.get().defaultBlockState().setValue(BlockScaffold.STRENGTH, 5)),
        block("Iron", Ic2Blocks.IRON_SCAFFOLD.get().defaultBlockState().setValue(BlockScaffold.STRENGTH, 5)),
        block("Iron braced", Ic2Blocks.IRON_SCAFFOLD.get().defaultBlockState().setValue(BlockScaffold.STRENGTH, 12))
    );
    private final List<Entry> ores = java.util.Arrays.stream(new String[]{
        "crushed_copper_ore", "crushed_gold_ore", "crushed_iron_ore", "crushed_lead_ore", "crushed_silver_ore", "crushed_tin_ore",
        "purified_crushed_copper_ore", "purified_crushed_gold_ore", "purified_crushed_iron_ore", "purified_crushed_lead_ore", "purified_crushed_silver_ore", "purified_crushed_tin_ore", "purified_crushed_uranium_ore"
    }).map(id -> new Entry(id.replace("purified_crushed_", "Clean ").replace("crushed_", "").replace("_ore", ""), new ItemStack(ic2.core.ref.Ic2Items.get(id)), null)).toList();
    private final boolean oreOnly;
    private int frames, checks, failed;
    private boolean complete, reported;

    private VisualGalleryScreen(boolean oreOnly) { super(Component.literal("IC2 visual gallery")); this.oreOnly = oreOnly; }

    public static void open(Minecraft mc) {
        active = new VisualGalleryScreen(false);
        mc.setScreen(active);
    }

    public static void openOres(Minecraft mc) {
        active = new VisualGalleryScreen(true);
        mc.setScreen(active);
    }

    public static void finish() {
        if (active == null) {
            IC2.LOG.error("[ic2-visual-gallery] FAIL gallery never opened");
            IC2.LOG.info("[ic2-visual-gallery] DONE 1 checks, 1 failed");
            return;
        }
        if (active.reported) return;
        if (!active.complete) active.check("gallery rendered and captured", false, active.frames);
        IC2.LOG.info("[ic2-visual-gallery] DONE {} checks, {} failed", active.checks, active.failed);
        active.reported = true;
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0xff0d1320);
        graphics.drawCenteredString(font, oreOnly ? "IC2: bundled user ore textures" : "IC2: machine fronts and scaffold faces", width / 2, 6, -1);
        var entries = oreOnly ? ores : machines;
        for (int i = 0; i < entries.size(); i++) draw(graphics, entries.get(i), machineX(i), machineY(i));
        if (!oreOnly) for (int i = 0; i < scaffolds.size(); i++) draw(graphics, scaffolds.get(i), scaffoldX(i), scaffoldY());
        graphics.flush();
        if (++frames >= 3 && !complete) {
            complete = true;
            try (NativeImage image = Screenshot.takeScreenshot(minecraft.getMainRenderTarget())) {
                Files.createDirectories(Path.of("audit"));
                image.writeToFile(Path.of("audit", oreOnly ? "ore_icons.png" : "machine_icons.png"));
                check("native GUI screenshot saved", true, image.getWidth() + "x" + image.getHeight());
                for (int i = 0; i < entries.size(); i++) inspect(image, entries.get(i), machineX(i), machineY(i));
                if (!oreOnly) for (int i = 0; i < scaffolds.size(); i++) inspect(image, scaffolds.get(i), scaffoldX(i), scaffoldY());
            } catch (Throwable error) {
                check("native GUI screenshot inspection completed", false, error.toString());
                IC2.LOG.error("[ic2-visual-gallery] screenshot failed", error);
            }
            finish();
        }
    }

    private void draw(GuiGraphics graphics, Entry entry, int x, int y) {
        graphics.fill(x, y, x + ICON_SIZE, y + ICON_SIZE, BACKGROUND);
        graphics.drawCenteredString(font, entry.label(), x + ICON_SIZE / 2, y + ICON_SIZE + 1, -1);
        graphics.pose().pushPose();
        if (entry.state() == null) {
            graphics.pose().translate(x, y, 0);
            graphics.pose().scale(2, 2, 1);
            // No durability/EU decorations: only the native item model can produce visible icon pixels.
            graphics.renderItem(entry.stack(), 0, 0);
        } else {
            // Reinforcement is a world state, not a second item: render its real baked block model in the GUI.
            var model = minecraft.getBlockRenderer().getBlockModel(entry.state());
            graphics.pose().translate(x + ICON_SIZE / 2f, y + ICON_SIZE / 2f, 150);
            graphics.pose().scale(ICON_SIZE, -ICON_SIZE, ICON_SIZE);
            model.getTransforms().getTransform(ItemDisplayContext.GUI).apply(false, graphics.pose());
            graphics.pose().translate(-.5f, -.5f, -.5f);
            minecraft.getBlockRenderer().renderSingleBlock(entry.state(), graphics.pose(), graphics.bufferSource(),
                LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        }
        graphics.pose().popPose();
    }

    private void inspect(NativeImage image, Entry entry, int x, int y) {
        double scale = minecraft.getWindow().getGuiScale();
        int visible = 0;
        int backgroundPixel = FastColor.ABGR32.fromArgb32(BACKGROUND);
        for (int py = (int)Math.ceil(y * scale); py < (int)Math.floor((y + ICON_SIZE) * scale); py++) {
            for (int px = (int)Math.ceil(x * scale); px < (int)Math.floor((x + ICON_SIZE) * scale); px++) {
                if (image.getPixelRGBA(px, py) != backgroundPixel) visible++;
            }
        }
        int minimum = (int)(50 * scale * scale);
        check(entry.label() + " native GUI pixels", visible > minimum, visible + " > " + minimum);
    }

    private int machineX(int i) { return 8 + (i % 5) * (width - 16) / 5 + ((width - 16) / 5 - ICON_SIZE) / 2; }
    private int machineY(int i) { return 20 + (i / 5) * 40; }
    private int scaffoldX(int i) { return 8 + i * (width - 16) / 4 + ((width - 16) / 4 - ICON_SIZE) / 2; }
    private int scaffoldY() { return 235; }
    private static Entry item(String label, Block block) { return new Entry(label, new ItemStack(block), null); }
    private static Entry block(String label, BlockState state) { return new Entry(label, ItemStack.EMPTY, state); }
    private record Entry(String label, ItemStack stack, BlockState state) {}

    private void check(String name, boolean ok, Object detail) {
        checks++;
        if (!ok) failed++;
        IC2.LOG.info("[ic2-visual-gallery] {} {} {}", ok ? "PASS" : "FAIL", name, detail);
    }
}
