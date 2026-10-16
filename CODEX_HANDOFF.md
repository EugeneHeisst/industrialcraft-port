# IC2 handoff from Codex — 2026-10-16

The user asked Codex to continue while the PC Claude session was at its five-hour limit, then update shared memory and deliver this handoff into that session. Claude had stopped reading the original crop system after commit `7087926` (terraformer). The work below is implemented, not a proposed plan.

## Implemented in 0.1.1

- Crop sticks and cross sticks, original 29 crop cards, growth rates/influences, environmental requirements, weighted crossing, inherited 0–31 stats, weed spread, harvest/regrowth, seed picking and trampling/poison interactions. Metal crops use modern `c:ores/<metal>` / `c:storage_blocks/<metal>` block tags, including deepslate variants, and stop rooting at air as in the original.
- Ported `ic2.api.crops` (`CropCard`, `Crops`, `BaseSeed`, `ICropTile`). Modern `Level`, `BlockPos`, biome tags and item components replace old Forge types. Registry identity remains `(owner, name)`, including original owner `IC2`. Addon crops have a dynamic renderer, so they do not require changing the builtin plant block-state enum. Crop textures belong in `textures/block/...` for the block atlas.
- Crop seed item with persistent/networked `ic2:crop_data`, identification and 4-stage scan data; original ItemCropSeed static accessors; creative tab seed variants.
- Crop analyzer, original EU costs 10/90/900/9000, battery slot, persistent handheld inventory, GUI and field inspection. Fixed handheld open-screen packet ordering in `IHandHeldInventory.open`: synchronize the initialized held item before the advanced open-screen payload.
- Cropmatron (fertilizer, 2,000 mB water, Weed-Ex), crop harvester (11×3×11 scan, optional analyzer for optimal harvest, ejector support), original GUIs and legacy recipe aliases.
- Fertilizer, Weed-Ex, hydration cell, weeding trowel, edible terra wart (cures original debuffs and subtracts 600 ticks of radiation). Original bucket/water-cell hydration, wheat-seed compost and bone meal interactions. Hydration cell is registered as original content; do not invent a recipe without checking the reference.
- Crop models/textures, translations, crafting/extractor recipes, dynamic redwheat lighting, and `minecraft:maintains_farmland` tag so dry planted farmland survives on 1.21.1.
- Electric sorting machine: six seven-item ghost whitelists, configurable default route, exact item-component matching, 20 EU/item, transformer upgrades, persisted filters, original GUI. Blocked filtered items never fall back to the default output. Ghost filters cannot consume/duplicate items.
- Item buffer: two isolated 24-slot banks, vertical/horizontal sided access, two independently routed ejector upgrades, original GUI and recipe.

## Validation and artifacts

- `JAVA_HOME=/usr/lib/jvm/java-21-openjdk JAVA_TOOL_OPTIONS=-Dic2.audit=server timeout 600 ./gradlew runServer -q`: **91 checks, 0 failed**, including the previous 62 checks. New checks are in `CropAudit` and `LogisticsAudit`, called before the main ServerAudit summary. Harvest audit retries complete harvests because a legitimate random roll can yield zero items.
- Hidden silent client: `./audit-client.sh` (Xvfb, OpenAL null, all volume sliders zero). Screenshots cover crops, crop analyzer, cropmatron, crop harvester, sorting machine and item buffer along with existing GUIs. See `run-audit/audit/` and the archived audit files under `versions/0.1.1/`.
- `./build.sh`, `git diff --check`.
- Preserved previous `0.1.0` JAR in `versions/0.1.0/`; new JAR is `versions/0.1.1/industrialcraft2-modern-0.1.1.jar`.
- Local commit only. No GitHub push/release and no Prism or production-server deployment: standing memory requires user review before publication. Repositories must remain private. Do not revert this batch when fetching/rebasing.

## Where to continue

Do **not** restart the crop port. IC2 is not finished. Still outstanding from Claude's prior list: stirling kinetic generator, lathe/turning blanks, advanced miner, CF sprayer/pack/foam, fuel jetpack, nuke/ITNT/dynamite, luminators, personal safe/trade-o-mat, booze/coffee, boats, JEI and final compatibility/audit/release work. `build/gen-warnings.txt` identifies recipes still skipped for missing content; preserve original legacy aliases in `tools/content.py`/`tools/gen.py`.

ASP and EMT remain project skeletons, Gravi Suit not started. Keep original user's order and cross-mod requirements from shared `memory/ic2_port.md`. Gravi reference jar is in the upload path recorded there. All test clients must remain hidden and silent. Never restart or kill the user's long-running production server; identify own JVMs by `/proc/<pid>/cwd`.

## Follow-up batch 0.1.2 — 2026-10-16 13:10 UTC

User asked Codex to keep working. Added fluid distributor (fair distribution / concentration, sided input reversal), electric fluid regulator (0–1000 mB/tick or second, 10 EU per successful transfer), condenser (100:1 steam to distilled water, passive 100 mB/tick or four vents, 2 EU/vent), and solar distiller (biome-temperature-dependent 36/72/144-tick rate, rain/night/roof checks). Added original textures, GUIs, legacy recipes, container slots, capabilities and persistence. Shared helper `FluidMachine` handles sided ports and transactional fluid/container transfers. Condenser completes its last batch even with empty input and preserves pending work when the output is full; fluid transfer refunds any destination remainder. Solar checks the chunk's skylight-source column directly, avoiding stale light propagation in freshly loaded chunks.

Server audit: **113 checks, 0 failed**. Hidden silent client completed with no errors; four new GUI screenshots inspected. Build/JAR and validation logs archived under `versions/0.1.2/`. This batch is also local-only, no push/release or production deployment.

Testing gotcha: do not compile new sources while a dev client/server is running from `build/classes/java/main`. Incremental compilation can temporarily remove inner classes and produce misleading runtime ClassNotFoundException. Run audits sequentially after compilation settles.

## Follow-up batch 0.1.3 — 2026-10-16

Added fluid heat generator, liquid heat exchanger and fermenter with original fluid/balance defaults, recipes and interfaces. Ported API `ILiquidAcceptManager`, `IFluidHeatManager`, `ILiquidHeatExchangerManager` and the mutable recipe maps; supports original fluid string aliases and modern namespaced IDs for addons. Fluid generator prepays a 20-tick burn batch and persists remaining fuel work. Exchanger has ten conductors (10 hU/t each), lava→pahoehoe lava and hot coolant→coolant at 20 hU/mB; refuses full/mismatched output without consuming input. Fermenter consumes 8,000 hU + 10 mB biomass for 200 mB biogas and produces fertilizer every 500 mB biomass; preserves work if fertilizer/gas output is blocked. All tanks/slots/partial work persist.

Server audit: **129 checks, 0 failed**, including a complete 500 mB biomass → 10,000 mB biogas + fertilizer run driven by the real fluid heat generator, heat/fuel accounting, idle behavior, returned coolant, full-output handling, addon API and recipes. Hidden silent client covers new GUIs with populated tanks; JAR/logs/screens archived under `versions/0.1.3/`. Still no GitHub push/release or production deployment. Steam boiler/kinetic generators are still outstanding.

## Follow-up batch 0.1.4 — 2026-10-16

Added pressure-controlled steam boiler and steam kinetic generator, turbine item (86,400 durability), original GUIs/assets/recipes. Boiler has 0–1,000 mB/t input, 0–300 bar, normal and superheated steam (220 bar / 374 C), heat intake, calcification, 500 C overheat explosion and blocked-exhaust pressure events. Turbine handles normal/superheated stages, condensate flooding, rotor wear, front-only kinetic output and side-only fluid ports; exhaust routes to condensers or second turbines. Steam/water types never flush each other during simulated input; pending exhaust and partial condensation persist. Kinetic output has a per-tick budget so multiple consumers cannot duplicate the same power. Boiler clamps the final partial water batch and holds pending steam instead of losing it.

Server audit **148 checks, 0 failed**: complete real heat→boiler→turbine→EU+condenser setup with exact steam-equivalent water balance, superheat, rotor wear, flooded output, input simulation, persistence, calcification and cancelled explosion-event audits for overheat/blocked exhaust. Hidden silent client passed, boiler/turbine GUIs inspected. JAR and logs/screens in `versions/0.1.4/`. No push/release/deployment.

The client audit now builds its stand at Y=120 with a platform and flight enabled: repeatedly reusing the old flat test world had eventually left the player falling below minimum build height, causing empty GUI lists and failed crop placement. This was an audit-world setup failure, not a crop registration regression.
