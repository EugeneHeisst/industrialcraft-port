"""Name table of the port: legacy IC2 names (InternalName / name@meta of the ini files) -> modern ids, textures, tags."""

# (modern id, legacy keys, texture path below textures/items of the original, lang key, tags)
SIMPLE_ITEMS = []

def item(id, legacy, tex, lang=None, tags=()):
    if isinstance(legacy, str): legacy = [legacy]
    SIMPLE_ITEMS.append(dict(id=id, legacy=legacy, tex=tex, lang=lang or legacy[0].split('@')[0], tags=list(tags)))

# dusts (itemDust@meta)
for meta, (name, inn, tag) in enumerate([
        ('bronze_dust', 'itemDustBronze', 'dusts/bronze'), ('clay_dust', 'itemDustClay', 'dusts/clay'), ('coal_dust', 'itemDustCoal', 'dusts/coal'),
        ('copper_dust', 'itemDustCopper', 'dusts/copper'), ('gold_dust', 'itemDustGold', 'dusts/gold'), ('iron_dust', 'itemDustIron', 'dusts/iron'),
        ('silver_dust', 'itemDustSilver', 'dusts/silver'), ('tin_dust', 'itemDustTin', 'dusts/tin'), ('hydrated_coal_dust', 'itemFuelCoalDust', 'dusts/hydrated_coal'),
        ('stone_dust', 'itemDustStone', 'dusts/stone'), ('lead_dust', 'itemDustLead', 'dusts/lead'), ('obsidian_dust', 'itemDustObsidian', 'dusts/obsidian'),
        ('lapis_dust', 'itemDustLapi', 'dusts/lapis'), ('sulfur_dust', 'itemDustSulfur', 'dusts/sulfur'), ('lithium_dust', 'itemDustLithium', 'dusts/lithium')]):
    item(name, [f'itemDust@{meta}', inn], f'resources/{inn}.png', inn, ['c:' + tag, 'c:dusts'])
for meta, (name, inn, tag) in enumerate([('silicon_dioxide_dust', 'itemDustsilicondioxide', 'dusts/silicon_dioxide'), ('diamond_dust', 'itemDustdiamond', 'dusts/diamond'),
        ('energium_dust', 'itemDustenergium', 'dusts/energium'), ('ashes', 'itemAshes', None)]):
    item(name, [f'itemDust2@{meta}', inn], f'resources/{inn}.png', inn, ['c:' + tag, 'c:dusts'] if tag else [])
for meta, (name, inn, tag) in enumerate([
        ('small_iron_dust', 'itemDustIronSmall', 'iron'), ('small_copper_dust', 'itemDustCopperSmall', 'copper'), ('small_gold_dust', 'itemDustGoldSmall', 'gold'),
        ('small_tin_dust', 'itemDustTinSmall', 'tin'), ('small_silver_dust', 'itemDustSilverSmall', 'silver'), ('small_lead_dust', 'itemDustLeadSmall', 'lead'),
        ('small_sulfur_dust', 'itemDustSulfurSmall', 'sulfur'), ('small_lithium_dust', 'itemDustLithiumSmall', 'lithium'), ('small_bronze_dust', 'itemDustBronzeSmall', 'bronze'),
        ('small_lapis_dust', 'itemDustLapiSmall', 'lapis'), ('small_obsidian_dust', 'itemDustObsidianSmall', 'obsidian')]):
    item(name, [f'itemDustSmall@{meta}', inn], f'resources/{inn}.png', inn, ['c:small_dusts/' + tag, 'c:small_dusts', 'c:tiny_dusts/' + tag])
# ingots (copper -> vanilla)
for meta, (name, inn, tag) in enumerate([(None, 'itemIngotCopper', 'copper'), ('tin_ingot', 'itemIngotTin', 'tin'), ('bronze_ingot', 'itemIngotBronze', 'bronze'),
        ('refined_iron_ingot', 'itemIngotAdvIron', 'refined_iron'), ('mixed_metal_ingot', 'itemIngotAlloy', 'mixed_metal'), ('lead_ingot', 'itemIngotLead', 'lead'), ('silver_ingot', 'itemIngotSilver', 'silver')]):
    if name: item(name, [f'itemIngot@{meta}', inn], f'resources/{inn}.png', inn, ['c:ingots/' + tag, 'c:ingots'] + (['c:ingots/steel'] if tag == 'refined_iron' else []))
METALS9 = ['copper', 'tin', 'bronze', 'gold', 'iron', 'refined_iron', 'lead', 'obsidian', 'lapis']
LEG9 = ['Copper', 'Tin', 'Bronze', 'Gold', 'Iron', 'AdvIron', 'Lead', 'Obsidian', 'Lapi']
for meta, (m, l) in enumerate(zip(METALS9, LEG9)):
    steel = m == 'refined_iron'
    item(f'{m}_plate', [f'itemPlates@{meta}', f'itemPlate{l}'], f'resources/itemPlate{l}.png', f'itemPlate{l}', ['c:plates/' + m, 'c:plates'] + (['c:plates/steel'] if steel else []))
    item(f'dense_{m}_plate', [f'itemDensePlates@{meta}', f'itemDensePlate{l}'], f'resources/itemDensePlate{l}.png', f'itemDensePlate{l}', ['c:dense_plates/' + m, 'c:dense_plates'] + (['c:dense_plates/steel'] if steel else []))
for meta, (m, l) in enumerate(zip(['copper', 'tin', 'bronze', 'gold', 'iron', 'refined_iron', 'lead'], ['Copper', 'Tin', 'Bronze', 'Gold', 'Iron', 'AdvIron', 'Lead'])):
    item(f'{m}_casing', [f'itemCasing@{meta}', f'itemCasing{l}'], f'resources/itemCasing{l}.png', f'itemCasing{l}', ['c:item_casings/' + m])
ORES7 = [('iron', 'Iron'), ('copper', 'Copper'), ('gold', 'Gold'), ('tin', 'Tin'), ('uranium', 'Uran'), ('silver', 'Silver'), ('lead', 'Lead')]
for meta, (m, l) in enumerate(ORES7):
    item(f'crushed_{m}_ore', [f'itemCrushedOre@{meta}', f'itemCrushed{l}Ore'], f'resources/itemCrushed{l}Ore.png', f'itemCrushed{l}Ore', ['c:crushed_ores/' + m])
    item(f'purified_crushed_{m}_ore', [f'itemPurifiedCrushedOre@{meta}', f'itemPurifiedCrushed{l}Ore'], f'resources/itemPurifiedCrushed{l}Ore.png', f'itemPurifiedCrushed{l}Ore', ['c:purified_crushed_ores/' + m])
for meta, (name, inn) in enumerate([('coil', 'itemPartCoil'), ('electric_motor', 'itemPartEleMotor'), ('power_unit', 'itempowerunit'), ('small_power_unit', 'itempowerunitsmall'),
        ('raw_crystal_memory', 'itemrawcrystalmemory'), ('heat_conductor', 'itemheatconductor'), ('copper_boiler', 'itemcopperboiler'), ('wood_rotor_blade', 'itemwoodrotorblade'),
        ('iron_rotor_blade', 'itemironrotorblade'), ('carbon_rotor_blade', 'itemcarbonrotorblade'), ('steel_rotor_blade', 'itemsteelrotorblade'), ('iron_shaft', 'itemironshaft'), ('steel_shaft', 'itemsteelshaft')]):
    item(name, [f'itemRecipePart@{meta}', inn], f'resources/{inn}.png', inn)
for name, inn, tex, tags in [
        ('resin', 'itemHarz', 'itemHarz', ['c:slimeballs/resin']), ('rubber', 'itemRubber', 'itemRubber', ['c:rubbers']),
        ('electronic_circuit', 'itemPartCircuit', 'itemPartCircuit', ['c:circuits/basic']), ('advanced_circuit', 'itemPartCircuitAdv', 'itemPartCircuitAdv', ['c:circuits/advanced']),
        ('advanced_alloy', 'itemPartAlloy', 'itemPartAlloy', ['c:plates/advanced_alloy']), ('carbon_fibre', 'itemPartCarbonFibre', 'itemPartCarbonFibre', []),
        ('carbon_mesh', 'itemPartCarbonMesh', 'itemPartCarbonMesh', []), ('carbon_plate', 'itemPartCarbonPlate', 'itemPartCarbonPlate', ['c:plates/carbon']),
        ('iridium_ore', 'itemOreIridium', 'itemOreIridium', ['c:gems/iridium']), ('iridium_plate', 'itemPartIridium', 'itemPartIridium', ['c:plates/iridium']),
        ('iridium_shard', 'itemShardIridium', 'itemShardIridium', []), ('coal_ball', 'itemPartCoalBall', 'itemPartCoalBall', []),
        ('compressed_coal_ball', 'itemPartCoalBlock', 'itemPartCoalBlock', []), ('coal_chunk', 'itemPartCoalChunk', 'itemPartCoalChunk', []),
        ('industrial_diamond', 'itemPartIndustrialDiamond', 'itemPartIndustrialDiamond', ['c:gems/diamond']), ('slag', 'itemSlag', 'itemSlag', ['c:slag']),
        ('scrap', 'itemScrap', 'itemScrap', []), ('plant_ball', 'itemFuelPlantBall', 'itemFuelPlantBall', []), ('biochaff', 'itemBiochaff', 'itemBiochaff', []),
        ('coin', 'itemCoin', 'itemCoin', []), ('grin_powder', 'itemGrinPowder', 'itemGrinPowder', []), ('weed', 'itemWeed', 'itemWeed', []),
        ('coffee_beans', 'itemCofeeBeans', 'itemCofeeBeans', []), ('coffee_powder', 'itemCofeePowder', 'itemCofeePowder', []), ('hops', 'itemHops', 'itemHops', []),
        ('tin_can', 'itemTinCan', 'itemTinCan', []), ('electrolyzed_water_cell', 'itemCellWaterElectro', 'cell/itemCellWaterElectro', []), ('cf_powder', 'itemPartCFPowder', 'itemPartCFPowder', []), ('fuel_rod', 'itemFuelRod', 'itemFuelRod', []), ('tritium_cell', 'itemTritiumCell', 'itemTritiumCell', []),
        ('steam_turbine_blade', 'itemSteamTurbineBlade', 'itemSteamTurbineBlade', []), ('painter', 'itemToolPainter', 'itemToolPainter', []),
        ('terraformer_blueprint', 'itemTFBP', 'itemTFBP', []), ('lithium_ingot', 'itemLithium', None, []), ('crystal_memory', 'itemcrystalmemory', 'resources/itemcrystalmemory', []),
        ('uranium_235', 'itemUran235', 'itemUran235', []), ('small_uranium_235', 'itemUran235small', 'itemUran235small', []), ('uranium_238', 'itemUran238', 'itemUran238', []),
        ('plutonium', 'itemPlutonium', 'itemPlutonium', []), ('small_plutonium', 'itemPlutoniumSmall', 'itemPlutoniumSmall', []),
        ('uranium_fuel', 'itemUran', 'itemUran', []), ('mox_fuel', 'itemMOX', 'itemMOX', []), ('rtg_pellet', 'itemRTGPellet', 'itemRTGPellet', []),
        ]:
    if tex is None: continue
    item(name, [inn], tex if '/' in tex else tex, inn, tags)
for it in SIMPLE_ITEMS:
    if not it['tex'].endswith('.png'): it['tex'] += '.png'

# Blocks: id, legacy keys, kind, extra
BLOCKS = []
def block(id, legacy, kind, **kw):
    if isinstance(legacy, str): legacy = [legacy]
    BLOCKS.append(dict(id=id, legacy=legacy, kind=kind, **kw))

# simple cubes: texture below textures/blocks
block('tin_ore', ['blockOreTin'], 'cube', tex='blockOreTin.png', tags=['c:ores/tin', 'c:ores', 'minecraft:mineable/pickaxe', 'minecraft:needs_stone_tool'], drop='self', lang='blockOreTin', item_tags=['c:ores/tin', 'c:ores'])
block('uranium_ore', ['blockOreUran'], 'cube', tex='blockOreUran.png', tags=['c:ores/uranium', 'c:ores', 'minecraft:mineable/pickaxe', 'minecraft:needs_iron_tool'], drop='self', lang='blockOreUran', item_tags=['c:ores/uranium', 'c:ores'])
block('lead_ore', ['blockOreLead'], 'cube', tex='blockOreLead.png', tags=['c:ores/lead', 'c:ores', 'minecraft:mineable/pickaxe', 'minecraft:needs_stone_tool'], drop='self', lang='blockOreLead', item_tags=['c:ores/lead', 'c:ores'])
for meta, (name, l, tag) in enumerate([(None, 'Copper', 'copper'), ('tin_block', 'Tin', 'tin'), ('bronze_block', 'Bronze', 'bronze'), ('uranium_block', 'Uranium', 'uranium'), ('lead_block', 'Lead', 'lead'), ('refined_iron_block', 'AdvIron', 'refined_iron')]):
    if name: block(name, [f'blockMetal@{meta}'], 'cube', tex=f'blockMetal{l}.png', tags=['c:storage_blocks/' + tag, 'minecraft:mineable/pickaxe', 'minecraft:needs_stone_tool'], drop='self', lang=f'blockMetal{l}', item_tags=['c:storage_blocks/' + tag])
block('reinforced_stone', ['blockAlloy'], 'cube', tex='blockAlloy.png', tags=['minecraft:mineable/pickaxe'], drop='self', lang='blockAlloy')
block('basalt', ['blockBasalt'], 'cube', tex='blockBasalt.png', tags=['minecraft:mineable/pickaxe'], drop='self', lang='blockBasalt')
block('machine', ['blockMachine@0', 'blockMachine'], 'cube', tex='machine/blockMachine.png', tags=['minecraft:mineable/pickaxe'], drop='self', lang='blockMachine')
block('advanced_machine', ['blockMachine@12'], 'cube', tex='machine/blockAdvMachine.png', tags=['minecraft:mineable/pickaxe'], drop='self', lang='blockAdvMachine')

# machines: id, legacy, texture sheet below textures/blocks, lang key, gui texture, drop when broken without wrench
MACHINES = [
    ('iron_furnace', 'blockMachine@1', 'machine/blockIronFurnace.png', 'blockIronFurnace', 'GUIFurnace', 'self'),
    ('electric_furnace', 'blockMachine@2', 'machine/blockElecFurnace.png', 'blockElecFurnace', 'GUIElecFurnace', 'self'),
    ('macerator', 'blockMachine@3', 'machine/blockMacerator.png', 'blockMacerator', 'GUIMacerator', 'machine'),
    ('extractor', 'blockMachine@4', 'machine/blockExtractor.png', 'blockExtractor', 'GUIExtractor', 'machine'),
    ('compressor', 'blockMachine@5', 'machine/blockCompressor.png', 'blockCompressor', 'GUICompressor', 'machine'),
    ('recycler', 'blockMachine@11', 'machine/blockRecycler.png', 'blockRecycler', 'GUIRecycler', 'machine'),
    ('generator', 'blockGenerator@0', 'generator/blockGenerator.png', 'blockGenerator', 'GUIGenerator', 'self'),
    ('geothermal_generator', 'blockGenerator@1', 'generator/blockGeoGenerator.png', 'blockGeoGenerator', 'GUIGenerator', 'self'),
    ('water_mill', 'blockGenerator@2', 'generator/blockWaterGenerator.png', 'blockWaterGenerator', 'GUIWaterGenerator', 'self'),
    ('solar_panel', 'blockGenerator@3', 'generator/blockSolarGenerator.png', 'blockSolarGenerator', 'GUISolarGenerator', 'self'),
    ('induction_furnace', 'blockMachine@13', 'machine/blockInduction.png', 'blockInduction', 'GUIInduction', 'advanced_machine'),
    ('metal_former', 'blockMachine2@4', 'machine/blockMetalFormer.png', 'blockMetalFormer', 'GUIMetalFormer', 'machine'),
    ('ore_washing_plant', 'blockMachine2@5', 'machine/blockOreWashingPlant.png', 'blockOreWashingPlant', 'GUIOreWashingPlant', 'machine'),
    ('thermal_centrifuge', 'blockMachine2@3', 'machine/blockCentrifuge.png', 'blockCentrifuge', 'GUITermalCentrifuge', 'advanced_machine'),
    ('block_cutter', 'blockMachine3@2', 'machine/blockBlockCutter.png', 'blockBlockCutter', 'GUIBlockCutter', 'machine'),
    ('blast_furnace', 'blockMachine3@1', 'machine/blockBlastFurnace.png', 'blockBlastFurnace', 'GUIBlastFurnace', 'machine'),
    ('solid_heat_generator', 'blockHeatGenerator@0', 'heatgenerator/blockSolidHeatGenerator.png', 'blockSolidHeatGenerator', 'GUISolidHeatGenerator', 'machine'),
    ('rt_heat_generator', 'blockHeatGenerator@2', 'heatgenerator/blockRTHeatGenerator.png', 'blockRTHeatGenerator', 'GUIRTHeatGenerator', 'machine'),
    ('electric_heat_generator', 'blockHeatGenerator@3', 'heatgenerator/blockElectricHeatGenerator.png', 'blockElectricHeatGenerator', 'GUIElectricHeatGenerator', 'machine'),
    ('rt_generator', 'blockGenerator@6', 'generator/blockRTGenerator.png', 'blockRTGenerator', 'GUIRTGenerator', 'machine'),
    ('semifluid_generator', 'blockGenerator@7', 'generator/blockSemifluidGenerator.png', 'blockSemifluidGenerator', 'GUIFluidGenerator', 'machine'),
    ('stirling_generator', 'blockGenerator@8', 'generator/blockStirlingGenerator.png', 'blockStirlingGenerator', 'GUIStirlingGenerator', 'machine'),
    ('kinetic_generator', 'blockGenerator@9', 'generator/blockKineticGenerator.png', 'blockKineticGenerator', 'GUIKineticGenerator', 'machine'),
    ('wind_kinetic_generator', 'blockKineticGenerator@0', 'kineticgenerator/blockWindKineticGenerator.png', 'blockWindKineticGenerator', 'GUIWindKineticGenerator', 'machine'),
    ('water_kinetic_generator', 'blockKineticGenerator@4', 'kineticgenerator/blockWaterKineticGenerator.png', 'blockWaterKineticGenerator', 'GUIWaterKineticGenerator', 'machine'),
    ('electric_kinetic_generator', 'blockKineticGenerator@2', 'kineticgenerator/blockElectricKineticGenerator.png', 'blockElectricKineticGenerator', 'GUIElectricKineticGenerator', 'machine'),
    ('manual_kinetic_generator', 'blockKineticGenerator@3', 'kineticgenerator/blockManualKineticGenerator.png', 'blockManualKineticGenerator', None, 'machine'),
    ('teleporter', 'blockMachine2@0', 'machine/blockTeleporter.png', 'blockTeleporter', None, 'advanced_machine'),
    ('tesla_coil', 'blockMachine2@1', 'machine/blockTesla.png', 'blockTesla', None, 'machine'),
    ('magnetizer', 'blockMachine@9', 'machine/blockMagnetizer.png', 'blockMagnetizer', 'GUIMagnetizer', 'machine'),
    ('electrolyzer', 'blockMachine@10', 'machine/blockElectrolyzer.png', 'blockElectrolyzer', 'GUIElectrolyzer', 'machine'),
    ('canner', 'blockMachine@6', 'machine/blockCanner.png', 'blockCanner', 'GUICanner', 'machine'),
    ('solid_canner', 'blockMachine2@9', 'machine/blockSolidCanner.png', 'blockSolidCanner', 'GUISolidCanner', 'machine'),
    ('fluid_bottler', 'blockMachine2@10', 'machine/blockFluidBottler.png', 'blockFluidBottler', 'GUIBottler', 'machine'),
    ('miner', 'blockMachine@7', 'machine/blockMiner.png', 'blockMiner', 'GUIMiner', 'machine'),
    ('pump', 'blockMachine@8', 'machine/blockPump.png', 'blockPump', 'GUIPump', 'machine'),
    ('mass_fabricator', 'blockMachine@14', 'machine/blockMatter.png', 'blockMatter', 'GUIMatter', 'advanced_machine'),
    ('wind_mill', 'blockGenerator@4', 'generator/blockWindGenerator.png', 'blockWindGenerator', 'GUIWindGenerator', 'self'),
    ('batbox', 'blockElectric@0', 'wiring/blockBatBox.png', 'blockBatBox', 'GUIElectricBlock', 'self'),
    ('cesu', 'blockElectric@7', 'wiring/blockCESU.png', 'blockCESU', 'GUIElectricBlock', 'self'),
    ('mfe', 'blockElectric@1', 'wiring/blockMFE.png', 'blockMFE', 'GUIElectricBlock', 'machine'),
    ('mfsu', 'blockElectric@2', 'wiring/blockMFSU.png', 'blockMFSU', 'GUIElectricBlock', 'machine'),
    ('lv_transformer', 'blockElectric@3', 'wiring/blockTransformerLV.png', 'blockTransformerLV', 'GUITransfomer', 'self'),
    ('mv_transformer', 'blockElectric@4', 'wiring/blockTransformerMV.png', 'blockTransformerMV', 'GUITransfomer', 'machine'),
    ('hv_transformer', 'blockElectric@5', 'wiring/blockTransformerHV.png', 'blockTransformerHV', 'GUITransfomer', 'machine'),
    ('chargepad_batbox', 'blockChargepad@0', 'wiring/blockChargepadBatBox.png', 'blockChargepadBatBox', 'GUIChargepadBlock', 'self'),
    ('chargepad_cesu', 'blockChargepad@1', 'wiring/blockChargepadCESU.png', 'blockChargepadCESU', 'GUIChargepadBlock', 'self'),
    ('chargepad_mfe', 'blockChargepad@2', 'wiring/blockChargepadMFE.png', 'blockChargepadMFE', 'GUIChargepadBlock', 'machine'),
    ('chargepad_mfsu', 'blockChargepad@3', 'wiring/blockChargepadMFSU.png', 'blockChargepadMFSU', 'GUIChargepadBlock', 'machine'),
    ('ev_transformer', 'blockElectric@6', 'wiring/blockTransformerEV.png', 'blockTransformerEV', 'GUITransfomer', 'machine'),
]
for id, leg, tex, lang, gui, drop in MACHINES:
    block(id, [leg], 'machine', tex=tex, lang=lang, gui=gui, drop=drop, tags=['minecraft:mineable/pickaxe'])

# Ore dictionary names of the original -> item tags
def oredict_tag(name):
    import re
    special = {'stickWood': 'c:rods/wooden', 'plankWood': 'minecraft:planks', 'logWood': 'minecraft:logs', 'treeLeaves': 'minecraft:leaves',
               'treeSapling': 'minecraft:saplings', 'slabWood': 'minecraft:wooden_slabs', 'stairWood': 'minecraft:wooden_stairs',
               'itemRubber': 'c:rubbers', 'woodRubber': 'c:rubber_woods', 'circuitBasic': 'c:circuits/basic', 'circuitAdvanced': 'c:circuits/advanced', 'itemResin': 'c:slimeballs/resin',
               'blockGlass': 'c:glass_blocks', 'paneGlass': 'c:glass_panes', 'gemDiamond': 'c:gems/diamond', 'gemEmerald': 'c:gems/emerald', 'gemLapis': 'c:gems/lapis',
               'gemQuartz': 'c:gems/quartz', 'dustRedstone': 'c:dusts/redstone', 'dustGlowstone': 'c:dusts/glowstone', 'cropWheat': 'c:crops/wheat',
               'record': 'minecraft:creeper_drop_music_discs', 'dye': 'c:dyes', 'craftingToolForgeHammer': 'ic2:tools/forge_hammer', 'craftingToolWireCutter': 'ic2:tools/wire_cutter',
               'plateAlloyAdvanced': 'c:plates/advanced_alloy', 'plateAlloyIridium': 'c:plates/iridium', 'plateCarbon': 'c:plates/carbon', 'gemIridium': 'c:gems/iridium',
               'ingotRefinedIron': 'c:ingots/refined_iron', 'ingotSteel': 'c:ingots/steel', 'dustNetherrack': 'c:dusts/netherrack', 'blockCobble': 'c:cobblestones',
               'cobblestone': 'c:cobblestones', 'sand': 'c:sands', 'stone': 'c:stones', 'woolWhite': 'minecraft:wool', 'slimeball': 'c:slime_balls'}
    if name in special: return special[name]
    for prefix, folder in [('crushedPurified', 'purified_crushed_ores'), ('crushed', 'crushed_ores'), ('dustSmall', 'small_dusts'), ('dustTiny', 'tiny_dusts'),
                           ('plateDense', 'dense_plates'), ('ingot', 'ingots'), ('ore', 'ores'), ('dust', 'dusts'), ('plate', 'plates'), ('gem', 'gems'),
                           ('block', 'storage_blocks'), ('nugget', 'nuggets'), ('itemCasing', 'item_casings'), ('casing', 'item_casings')]:
        if name.startswith(prefix) and len(name) > len(prefix) and name[len(prefix)].isupper():
            mat = name[len(prefix):]
            mat = re.sub(r'(?<!^)(?=[A-Z])', '_', mat).lower()
            mat = {'adv_iron': 'refined_iron', 'lapi': 'lapis', 'uran': 'uranium', 'lapis_lazuli': 'lapis'}.get(mat, mat)
            return f'c:{folder}/{mat}'
    return None
