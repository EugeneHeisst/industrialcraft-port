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

Do **not** restart the crop port. IC2 is not finished. Still outstanding from Claude's prior list: steam generator, fermenter, liquid heat exchanger, fluid heat generator, steam/stirling kinetic generators, lathe/turning blanks, advanced miner, CF sprayer/pack/foam, fuel jetpack, nuke/ITNT/dynamite, luminators, personal safe/trade-o-mat, booze/coffee, boats, JEI and final compatibility/audit/release work. `build/gen-warnings.txt` identifies recipes still skipped for missing content; preserve original legacy aliases in `tools/content.py`/`tools/gen.py`.

ASP and EMT remain project skeletons, Gravi Suit not started. Keep original user's order and cross-mod requirements from shared `memory/ic2_port.md`. Gravi reference jar is in the upload path recorded there. All test clients must remain hidden and silent. Never restart or kill the user's long-running production server; identify own JVMs by `/proc/<pid>/cwd`.

## Follow-up batch 0.1.2 — 2026-10-16 13:10 UTC

User asked Codex to keep working. Added fluid distributor (fair distribution / concentration, sided input reversal), electric fluid regulator (0–1000 mB/tick or second, 10 EU per successful transfer), condenser (100:1 steam to distilled water, passive 100 mB/tick or four vents, 2 EU/vent), and solar distiller (biome-temperature-dependent 36/72/144-tick rate, rain/night/roof checks). Added original textures, GUIs, legacy recipes, container slots, capabilities and persistence. Shared helper `FluidMachine` handles sided ports and transactional fluid/container transfers. Condenser completes its last batch even with empty input and preserves pending work when the output is full; fluid transfer refunds any destination remainder. Solar checks the chunk's skylight-source column directly, avoiding stale light propagation in freshly loaded chunks.

Server audit: **113 checks, 0 failed**. Hidden silent client completed with no errors; four new GUI screenshots inspected. Build/JAR and validation logs archived under `versions/0.1.2/`. This batch is also local-only, no push/release or production deployment.

Testing gotcha: do not compile new sources while a dev client/server is running from `build/classes/java/main`. Incremental compilation can temporarily remove inner classes and produce misleading runtime ClassNotFoundException. Run audits sequentially after compilation settles.
