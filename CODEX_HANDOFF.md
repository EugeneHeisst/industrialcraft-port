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

Server audit **275 checks, 0 failed** (FoamAudit: sprayer modes/amounts, collision, hardening, sand, painting, reinforced chain, cable foam full cycle incl. persistence and breaking, CF pack priority, recipes). Hidden silent client passed; foamed cable stand (soft, light gray, blue) inspected. Artifacts in versions/0.1.10. Pushed to the private repo only; no release/install/deploy.

Next: dynamite (PointExplosion, sticky, remote, dispenser), personal safe / trade-o-mat / energy-o-mat, booze/coffee, boats, containment box / obscurator / upgrade kit, JEI; then ASP → EMT → Gravi Suit.

## Claude batch 0.1.11 — dynamite

Added dynamite following IC2 1.7.10: `PointExplosion` (3×3×3 cube around the stick, breaks blocks with resistance < power×10, drops at the drop rate, 20 damage to entities within 2 blocks, posts the cancellable `ExplosionEvent`), `EntityDynamite` (100-tick fuse, sticky sticks attach and burn 4× faster, water defuses, fuse/sticky/stick state persisted), `BlockDynamite` blocks `dynamite` / `dynamite_remote` (standing or wall-hung torch-style models; breaking, redstone or the remote ignite with a 40-tick fuse, a nearby explosion with 5 ticks; losing all support sets it off, losing its own support removes it), items `dynamite` (places on a block, throws in the air), `sticky_dynamite` (throws), `dynamite_remote` (sneak-free click on a stick links/unlinks it and turns it into the remote variant; right click in the air sets off every linked stick). Both dynamite items are `ProjectileItem` with dispenser behaviour. Entity `ic2:dynamite` uses the thrown-item renderer.

Server audit **286 checks, 0 failed** (DynamiteAudit: placing/wall-hanging, remote link/unlink/detonation, block and entity explosion effects with obsidian spared, redstone ignition, support loss, sticky fuse rate, recipes/dispenser). Hidden silent client passed; standing, remote and hovering thrown sticks inspected in cables.png. Artifacts in versions/0.1.11. Pushed to the private repo only.

Next: personal safe / trade-o-mat / energy-o-mat, booze/coffee, boats, barrel, containment box / obscurator / upgrade kit, JEI; then ASP → EMT → Gravi Suit.

## Claude batch 0.1.12 — personal safe, trade-o-mat, energy-o-mat

Added `personal_chest`, `trade_o_mat`, `energy_o_mat` (legacy blockPersonal@0/1/2, IC2 1.12 ids) in package `ic2.core.block.personal`. All three are unbreakable (hardness -1, resistance 6,000,000, `canEntityDestroy` false, push reaction BLOCK) and only the owner (or an operator) can wrench them; the safe only when empty. Ownership is stored as the original `ownerGameProfile` tag; the placer now owns the block at once (the original let whoever opened it first claim it). The safe (54 slots, Access NONE, no null-side item capability) renders through `RenderPersonalChest` (ModelPersonalChest port, newsafe.png, door swings with the block-event user count) and the same model is used for the item via BEWLR.

Trade-o-mat: demand/offer template slots, linked input (`InvSlotConsumableLinked`), output; a trade takes exactly the offer from adjacent inventories and puts the payment into them, personal blocks first; a personal block is only reachable from a personal block of the same owner (`StackUtil.getAdjacentInventories/fetch/distribute`). Unlike 1.7.10 (whose sided checks blocked it) the owner's safe really works as stock and till. Operators get the ∞ button (infinite stock, payment consumed). Energy-o-mat: each paid demand stack adds euOffer EU (owner buttons ±100…±100k, min 100) to paidFor; takes EU from every side but the front, emits through the front at its tier, charges the charge slot; energy storage / transformer upgrades. Owner vs customer GUIs are chosen on the server and sent with the menu (`IHasGui.writeGuiData` / `getGuiContainer(id, inv, buf)`). Long Russian labels are scaled (`GuiIC2.drawFit`). The o-mat trade sound is `machines.o_mat`.

Also: the dynamite audit area chunks are now forced at audit start (in a reloaded world, entities spawned in a non-ticking chunk are invisible to getEntitiesOfClass).

Server audit **312 checks, 0 failed** (PersonalAudit: ownership, guest refusal, sealed against hoppers, unbreakable/blast proof, wrench rules, persistence, trading through the owner's safe, foreign safe ignored, full-stack demand, stock, infinite mode/op only, owner/customer views, energy payment/acceptance/emission/charging, price buttons, upgrades, recipes). Hidden silent client: owner and customer GUIs of both o-mats, safe GUI, personal.png stand (closed safe, opening safe, both o-mats, item renders) inspected. Artifacts in versions/0.1.12. Pushed to the private repo only.

Next: booze/coffee, boats, barrel, containment box / obscurator / upgrade kit, itemRecipePart@5, JEI; then ASP → EMT → Gravi Suit.

## Claude batch 0.1.13 — booze barrel, mugs, coffee

Items `empty_mug` (itemMugEmpty), `cold_coffee` / `dark_coffee` / `coffee` (itemMugCoffee@0/1/2), `booze_mug` (itemMugBooze) and `barrel` (itemBarrel); block `barrel` (no block item, `tap` = none/north/south/west/east, loot = scaffold + empty barrel). The packed booze value of the original item damage lives in the `ic2:booze` component (mug and barrel item), the booze model picks beer age / rum by the `ic2:booze` item property. Barrel on a wooden scaffold: water bucket/cells, wheat and hops brew beer (ages Brew → Youngster → Beer → Ale → Dragonblood at 24000·3^x ticks, x = 0,1,2,3,6; bad recipes spoil to Black Stuff), sugar cane ferments rum (timeNedForRum); aging stops while a treetap is fitted; a mug on the tapped side draws one litre; left click removes the tap (spilling a litre) or takes the filled barrel off; the barrel item keeps its brew. Effects follow ItemBooze/ItemMugCoffee with modern effect holders. Cold coffee smelts to dark coffee (0.1 xp). The original's unreachable solid-ratio-1 test is kept.

Server audit **329 checks, 0 failed** (BoozeAudit). Hidden silent client: booze.png (tapped/untapped barrel, all beer ages, rum, coffee, mug icons) inspected. Artifacts in versions/0.1.13. Pushed to the private repo only.

Next: boats, containment box / obscurator / upgrade kit, itemRecipePart@5, JEI; then ASP → EMT → Gravi Suit.

## Claude batch 0.1.14 — boats

Items `carbon_boat`, `rubber_boat`, `broken_rubber_boat`, `electric_boat` (itemBoat@0-3) and entities `ic2:carbon_boat` / `rubber_boat` / `electric_boat` (`ic2.core.item.boat`). The boats extend the modern `Boat` (its paddling/physics) and keep IC2's differences: crashing faster than the break speed (carbon 0.4, rubber 0.23, electric 0.5) destroys the boat — the rubber boat pops and tears (broken rubber boat) above 0.26, the others drop themselves. Crashes are detected where the boat is simulated: server for riderless boats, the steering client otherwise (sent as `ic2:boat_crash`, validated against the controlling passenger). Electric boat: fire immune, floats on lava (`canBoatInFluid`), pays 4 EU per tick from the rider's armour while going forward (synced `ACCELERATED`), then gets twice the paddle force up to 0.7 blocks/tick; without charge it crawls. Rendered with the 1.7.10 boat model and the original boat textures. NeoForge fake players cannot ride, so the server audit checks the payment directly and the client audit drives the real player.

Server audit **341 checks, 0 failed** (BoatAudit: placing, broken boat not placeable, drops/pick, crash per kind/speed, lava/fire, EU payment, coasting, recipes incl. repair). Hidden silent client with the real player: electric boat 0.61 blocks/tick using armour EU, rubber boat driven into a wall pops (broken item), electric boat floats on lava unburnt; electric_boat.png / lava_boat.png inspected. Artifacts in versions/0.1.14. Pushed to the private repo only.

Next: containment box / obscurator / upgrade kit, itemRecipePart@5, JEI; then ASP → EMT → Gravi Suit.

## Claude batch 0.1.15 — obscurator, containment box, MFSU upgrade kit

`obscurator` (100,000 EU, tier 2, 250 EU/t): sneak-click a full opaque model block to scan the clicked side (20,000 EU, kept as `referencedBlock` state + `referencedSide` in custom data, tooltip names it), click a side of hardened construction foam to give that side the look (5,000 EU) through the new API event `ic2.api.event.RetextureEvent`. Handlers: foam wall (now has a block entity `ic2:foam_wall` / `TileEntityWall` and a `retextured` state that hides its model so `RenderFoamWall` draws all sides) and hardened cable foam (`TileEntityCable.retexture`, cleared when the foam goes soft/away, drawn by `RenderCableFoam`). `ic2.core.block.Retexture` keeps six state/side pairs (NBT `retexture`); `RetextureRender` draws each side from the referenced model's face quads with block tints (grass etc.). Walls from older versions get their block entity on first retexture. No overlay on the obscurator icon (the original drew the scanned texture on it).

`containment_box`: hand-held 12-slot inventory (GUIContainmentbox) accepting radioactive items only; items inside no longer irradiate the carrier. `mfsu_upgrade_kit` (itemupgradekit@0): on an MFE or MFE charge pad it becomes the MFSU version in place with energy, facing, redstone mode and items kept (nothing drops). The generator now strips `@<meta>*` wildcards, so both solid heat generator recipes exist; remaining gen warnings are only the blockHarz name and the BuildCraft UU value.

Server audit **355 checks, 0 failed** (ObscuratorAudit). Hidden silent client: obscurator.png (log/grass(tinted)/brick sides on walls and a foamed cable, plain yellow wall) and gui_containment_box.png inspected. Artifacts in versions/0.1.15. Pushed to the private repo only.

Next: JEI integration, final IC2 compatibility pass; then ASP → EMT → Gravi Suit.

## Claude batch 0.1.16 — JEI

`ic2.core.jei.IC2JeiPlugin` (JEI stays compileOnly; run the client audit with `./audit-client.sh -PwithJei=true` to include it). Pages for every IMachineRecipeManager (macerator, extractor, compressor, thermal centrifuge with heat, ore washing with water, block cutter with hardness, blast furnace, the three metal former modes, UU amplifier with amplification), solid canning, canner fluid enrichment (fluid ingredients), and scrap box drops with chances; catalysts include the iron/electric/induction furnaces for smelting. IC2 crafting recipes (`ic2:shaped/shapeless`) already show under crafting. Labels `ic2.jei.*` in en/ru.

Server audit 355/355 unchanged. Hidden silent client with JEI: all eight pages open (macerator, thermal centrifuge, ore washing, rolling, solid canning, enrichment, scrap box, MFSU crafting) and were inspected. Artifacts in versions/0.1.16. Pushed to the private repo only.

Next: IC2 final compatibility pass (API surface ASP/EMT/Gravi need), then ASP → EMT → Gravi Suit.

## Claude batch 0.1.17 — multiplayer jetpack flight validated

Ported `Platform.resetPlayerInAirTime` as `Util.resetPlayerInAirTime` (reflection on `ServerGamePacketListenerImpl.aboveGroundTickCount`); the fuel jetpack, electric jetpack and quantum chestplate call it while their thrust works. New `./mp-flight-audit.sh`: starts the dedicated dev server (`run-server`, allow-flight=false) with `-Dic2.audit=mpflight` and a hidden silent dev client (`runMpclient`, joins 127.0.0.1:25581 — "localhost" resolved to ::1 and timed out) holding jump. Result: 240 ticks of hover-mode climbing on a charged electric jetpack (20.7 blocks), floating counter never above 1, no kick; control phase with the reset disabled → "IC2Flyer was kicked for floating too long!" after 81 ticks and the client shows "Flying is not enabled on this server". This closes the multiplayer flight item from the Codex handoff. Logs in versions/0.1.17 (mp-server-audit.log, mp-client-audit.log). Server audit 355/355.

Next: ASP → EMT → Gravi Suit.

## Codex batch 0.1.18 — JEI startup deadlock found during ASP port

JEI discovers plugins concurrently with mod construction. The eager IC2JeiPlugin.MACHINES initializer touched Ic2Blocks while IC2's own constructor initialized Ic2Fluids, deadlocking both class initializers (confirmed by /tmp/asp-client-threads.txt, archived under versions/0.1.18). Machine descriptors now initialize lazily when JEI registers categories, after mod construction. No recipe changes.

Build passed. ASP 0.1.0 + IC2 0.1.18 dedicated server: 53/53 checks, real EU transfers. Hidden silent client with JEI loaded successfully three times after the fix; ASP molecular and IC2 macerator pages both opened. Latest client log and macerator screenshot in versions/0.1.18. Local commit only, no push/release/install. ASP now requires this version; continue from its CODEX_HANDOFF.md, then EMT → Gravi Suit.

## Claude 0.1.19 — per-stack tool energy cost API

`ItemElectricTool.getEnergyCost(ItemStack)` (default operationEnergyCost); getDestroySpeed/mineBlock use it. For addons with per-stack modes (the Gravi advanced drill already has a matching method) and for the EMT TC-bore bridge, which should switch from the `operationEnergyCost` field to this method. Server 355/355. Archive versions/0.1.19. Gravi/EMT/ASP are still built against 0.1.18 — bump them in the compat batch.

## 0.1.20 — ores in high terrain

Each IC2 ore now also has a sparse upper layer (`<ore>_upper` placed features, triangular 64..320, same two-target configured feature): tin 10 veins, uranium 3, lead 4 per chunk, so mountains above sea level hold IC2 ore like vanilla iron/copper. Lower layer unchanged. `-Dic2.audit=worldgen` (`./gradlew runWorldgen`, fresh world, seed ic2audit) now reports 32-block bands across the whole height and checks the nearest mountain biome: all three ores found at y >= 64 (PASS), deepslate checks still PASS. Old test world kept as run-wg/world.before-upper-ores. Artifacts in versions/0.1.20.


## Codex 2026-10-17 — 0.1.23, charged shears before approved deployment

Chainsaw SHEARS_DIG now requires enabled shearing and one full operation of EU. Other native shears abilities are no longer advertised without an EU-paying implementation. Empty sheep interaction returns PASS; charging from worn armor remains supported. Added real native leaf/shee/pumpkin tests for every registered chainsaw subtype. Standalone server363/363 passed; final jar archived versions/0.1.23. Combined full-pack server29/29 and client14/14 passed with the new set. Combined IC2 audit also covers Gravi's inherited chainsaw.

User explicitly approved both GitHub releases and installation in latest Claude history ("Оба"); Codex continues that authorized release/deploy work. The earlier no-publish/no-install handoff condition was satisfied by this separate approval. Latest release/install state will be in ../CLAUDE_CONTINUE_PORTS.md.


## Codex isolated review — 0.1.24, 2026-10-17

Branch codex/fall-absorption, worktree ~/Project/codex-reviews/industrialcraft2-modern. Main/source and production untouched; prepared for Claude integration.

- Native Nano/Quantum fall fallback restored before BYPASSES_ARMOR guard; original25-scale charge budget, Nano .875 long-fall ratio, Quantum1 and floor/min-one payment. Existing funded full-cancel path retained.18 realServerPlayer.causeFallDamage cases: baseline381checks/8fail, fixed all18pass. Ordinarymelee handling unchanged.
- Energy injection and signed source draw in ElectricMachine/ElectricBlock mark persistent state dirty. ASPnativeaudit separates receiver/sourcechunks to expose missingdirtycalls.
- Liquid slot now drains at most tankacceptance, preserving partialcontainer and customdata. Seven actualbottler/capability cases:30Bjetpack→8Btank, final1mB, simulation/customname, blockedoutput/fulltank/mixing/atomicvanillabucket. Three initialfailures reproduced; final388/388.
- JAR/build/SHA/server/baseline logs versions/0.1.24 in isolatedworktree. No push/release/install. IC2pendingoriginalparity queue: ~/Project/compat-test/IC2_REVIEW_NEXT.md.
