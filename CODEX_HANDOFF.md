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

Do **not** restart the crop port. IC2 is not finished. Still outstanding from Claude's prior list: CF sprayer/pack/foam, dynamite/remote, personal safe/trade-o-mat/energy-o-mat, booze/coffee, boats, containment box/obscurator/upgrade kit, JEI and final compatibility/audit/release work. `build/gen-warnings.txt` identifies recipes still skipped for missing content; preserve original legacy aliases in `tools/content.py`/`tools/gen.py`.

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


## Follow-up batch 0.1.5 — 2026-10-16

Added lathe, iron/wood turning blanks, lathing tool and Stirling kinetic generator. Ported ILatheItem/ILatheTool/ICustomDamageItem APIs, persistent/networked five-section shape component, original recipes and material assets. Lathe takes real adjacent kU, friction, 1,000 kU/cut, tool hardness and fifty-use durability, randomized byproducts, full-output protection. Invalid section events are rejected. Custom item renderer reflects each cut in inventory/hand/world. Shape-sensitive recipes now use NeoForge component ingredients for the four original shaft/handle/drill forms; uncut blanks cannot substitute. The formerly over-broad blockMachine3 fallback generated a bogus alternate boiler recipe for the lathe; the explicit lathe alias now removes it and creates the correct recipe.

Stirling takes heat on all non-output sides, turns four hU into twelve kU plus one hU of fluid warming, has 2,000 mB working tanks and persistent buffers. Original water→hot water and addon one-way heatup fluids are supported; reversible coolant is excluded. Fixed original KU overfill and mismatched kUBuffer/kubuffer save keys. Front-only KU is consumed once; full or incompatible output preserves input and unused heat.

Server audit **168 checks, 0 failed**, including both electric motor→lathe and real heat exchanger→Stirling→EU/lathe chains, exact working-fluid balance, shape recipes, durability and persistence. Hidden silent client passed, both new GUI screenshots inspected; JAR/logs/screens archived under versions/0.1.5. No push/release/deployment. Next: advanced miner.


## Follow-up batch 0.1.6 — 2026-10-16

Added advanced miner with OD 17×17 / OV 33×33 layer scanning, 512 EU per mined block, 64 scanner EU per examined cell, five cells per 20 ticks with overclocker augmentation, internal scanner charging, 4M EU buffer and transformer/redstone inverter upgrades. Filter inventory supports blacklist/whitelist of actual drops; Silk Touch uses modern loot tables. Actual mining distributes to adjacent capabilities and spills remainder above the machine. Skips liquids/unbreakable blocks/non-ore block entities and posts cancellable BreakEvent using a fake player for claims/protection. Pauses at unloaded columns and never force-loads chunks. Modern height support reaches Y=-64 and persists negative layer plus exact scan cursor/timing. Original GUI, assets and recipe are registered.

Server audit **192 checks, 0 failed**, including real loot/energy accounting, filters/Silk Touch, cancelled protection event, chest preservation, redstone, scanner exhaustion, upgrade scan budget/tier, height floor and saved cursor. Hidden silent client passed; advanced miner GUI inspected. JAR/logs/screens archived versions/0.1.6. No publication/deployment. Next: industrial TNT and nuke.


## Follow-up batch 0.1.7 — 2026-10-16

Added industrial TNT (60-tick fuse, power 5.5, 90% normal blast drops, redstone/fire/projectile/manual/chain ignition) and nuclear bomb with original eight mirrored outer charge slots + inner fuel GUI. All uranium/plutonium variants use original enrichment thresholds/formulas and radiation multipliers; original default power cap 60 and enableNuke are NeoForge SERVER config values. Nuke normal fuse is 300 ticks; chains use shortened fuse, wrench defuses active nuke without returning fuel. Charge is captured and cleared before removing casing, avoiding content drops/duplication. Primed entity uses modern TNT synchronization/render/motion and persists power/drop/radiation/owner/fuse (including 300 ticks), fixing original byte fuse/save loss. Nuke supports redstone and modern flint/fire-charge ignition. Correct six-face original textures are retained. ITNT loot is empty because breaking it ignites it; no intact duplicate charge.

Server audit **218 checks, 0 failed**, including original formula cases, config disable/cap, mirrored-slot shift-click accounting, ignition, persistence, defuse, redstone/chain, cancelled nuke ExplosionEvent, real small ITNT blast and mob radiation/hazmat. Hidden silent client passed; nuke GUI and both primed models inspected. JAR/logs/screens versions/0.1.7. No push/release/deploy. Next: luminator and fuel jetpack.


## Follow-up batch 0.1.8 — 2026-10-16

Added luminator (2 EU buffer, 1 EU/4 ticks, light 15, supporting cable input only, wrench toggles redstone polarity, support-loss removal), six mounting directions and original narrow/full-cover shapes and textures. Added fuel jetpack (30,000 mB biogas, 2 mB/t flight / 1 hover, low-fuel thrust reduction below 20%, modern world-height ceiling, persisted hover mode) and reusable fluid armor tank component/capability. Actual bottler refuels it. Restored electric jetpack's original 8 EU flight / 7 EU hover costs and no ground consumption. Luminator legacy aliases restore scanner/nightvision recipes. Fixed missed external FluidHandler registration for Stirling from 0.1.5, tested both fill and output drain through actual capabilities.

Server audit **244 checks, 0 failed** including real BatBox→glass cable→luminator energy network, lamp polarity/light/persistence/support, fluid capacity/simulation/persistence, flight/hover/low-fuel/ceiling/key mode, electric consumption, real bottler and Stirling ports. Hidden silent client passed; populated lamp stand and worn/filled jetpack inspected. JAR/logs/screens versions/0.1.8. No publication/deployment. Next: four automatic charging batteries.


## Follow-up batch 0.1.9 — final takeover handoff

Added four automatic hotbar charging batteries: RE 40,000 EU / 128 per cycle / tier1; advanced 400,000 / 1,024 / tier2; energy crystal 4,000,000 / 8,192 / tier3; lapotron 40,000,000 / 32,768 / tier4. Every ten ticks, one shared transfer budget across nine hotbar slots, tier/target charge limits enforced, charging batteries excluded as targets. Three persistent right-click modes ENABLED/DISABLED/NOT_IN_HAND; toolbox permits only disabled batteries. Original five-step charge icons, names and all four recipes retained, crafting inherits EU through existing AdvRecipes.

Server audit **259 checks, 0 failed**: shared budget/EU conservation, selected-slot exclusion, target tiers, battery-to-battery exclusion, hotbar boundary, partial final charge, all mode transitions and save data, automatic ten-tick schedule, all capacities/rates/recipes. Hidden silent client and build pass; full/empty battery models inspected. Artifacts in versions/0.1.9.

## Authoritative resume point

Codex finished nine local batches 0.1.1–0.1.9 after Claude's 7087926, all on main. The latest version is 0.1.9, 259 server checks, hidden silent client/build passed. **No GitHub push/release, Prism install or production-server changes.** Shared memory and this real Claude session received handoff prompts throughout. Use the latest section and the remaining list above; earlier "Next" lines are historical.

Continue remaining IC2 content, then ASP → EMT → Gravi Suit and cross-mod validation. First useful next batch is CF system: sprayer, 80,000 mB pack (reuse ItemArmorFluidTank), foam/reinforced foam and hardening/colored walls, cable foam state/rendering. Dynamite requires the original PointExplosion behavior, placed/remote/sticky variants and dispenser integration; it is NOT implemented by the ITNT/nuke batch. Other missing recipes are listed in build/gen-warnings.txt; that file is not an exhaustive content inventory. Containment box, obscurator and upgrade kit remain unimplemented. ASP/EMT are still skeletons; Gravi Suit is not started.

Final multiplayer flight validation remains necessary for both jetpacks: the current thrust/consumption audits use a fake player and do not test vanilla's >80-tick floating kick on a default allow-flight=false server. Inspect/reset the connection's floating counter through an appropriate modern hook/accessor if needed (old IC2 resetPlayerInAirTime did this). Do not claim full multiplayer validation from the current flight unit checks. Full-mod completeness and addon compatibility are not yet established.

## Claude batch 0.1.10 — construction foam (resumed after the takeover)

Added the CF system: CF sprayer (8,000 mB construction foam, 100 mB per block, 10-block spreading away from the player or single mode, fills from CF fluid sources), CF pack (80,000 mB, reuses ItemArmorFluidTank, the sprayer drains it first), foam / reinforced foam (walk-through, harden after light-gated scheduled checks or at once with sand; reinforced → reinforced stone, drops an iron scaffold), sixteen-colour foam wall (painter recolours, picks as foam, drops nothing), CF powder turns water sources into construction foam. Block ids follow IC2 1.12 (`foam`, `reinforced_foam`, `foam_wall`) because `construction_foam` is the fluid block.

Cable foam: `TileEntityCable.foamed/foamColor` persisted and synced; soft foam has no collision, hard foam is a full opaque block (light block 15, 90 resistance, hardness 3); breaking a foamed cable removes only the foam; painting hardened cable foam colours the wall, not the insulation. Rendered by a cable BER (`RenderCableFoam`) drawing the foam/wall block model; cable blocks are now `dynamicShape` so shape/light depend on the BE.

Also fixed a cable colour bug from the original Claude port: COLOR is the dye index (1 red … 15 white, 0 plain), but the generator had mapped COLOR n to the texture of dye n−1, so painted cables showed the wrong colour.

Server audit **275 checks, 0 failed** (FoamAudit: sprayer modes/amounts, collision, hardening, sand, painting, reinforced chain, cable foam full cycle incl. persistence and breaking, CF pack priority, recipes). Hidden silent client passed; foamed cable stand (soft, light gray, blue) inspected. Artifacts in versions/0.1.10. Local commit only; no push/release/deploy.

Next: dynamite (PointExplosion, sticky, remote, dispenser), personal safe / trade-o-mat / energy-o-mat, booze/coffee, boats, containment box / obscurator / upgrade kit, JEI; then ASP → EMT → Gravi Suit.
