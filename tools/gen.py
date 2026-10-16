#!/usr/bin/env python3
"""Generates the port's resources from the original IC2 jar (related/ic2-reference/jar) and content.py."""
import json, os, re, shutil, sys
from PIL import Image
import content

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
JAR = os.path.join(ROOT, 'related/ic2-reference/jar')
OTEX = os.path.join(JAR, 'assets/ic2/textures')
RES = os.path.join(ROOT, 'src/generated/resources')
A = os.path.join(RES, 'assets/ic2')
D = os.path.join(RES, 'data')
warnings = []

def write(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w', encoding='utf8') as f:
        json.dump(obj, f, indent=2, ensure_ascii=False) if not isinstance(obj, str) else f.write(obj)

def copy(src, dst):
    os.makedirs(os.path.dirname(dst), exist_ok=True)
    shutil.copyfile(src, dst)
    if os.path.exists(src + '.mcmeta'): shutil.copyfile(src + '.mcmeta', dst + '.mcmeta')

def props(name):
    ret = {}
    for line in open(os.path.join(JAR, 'ic2/lang', name), encoding='utf8'):
        line = line.strip()
        if not line or line.startswith('#') or '=' not in line: continue
        k, v = line.split('=', 1)
        ret[k.strip()] = v.strip()
    return ret

shutil.rmtree(RES, ignore_errors=True)
LANG = {'en_us': props('en_US.properties'), 'ru_ru': props('ru_RU.properties')}
out_lang = {k: {} for k in LANG}
for code, src in LANG.items():
    for k, v in src.items():
        out_lang[code][k if k.startswith('itemGroup') or k.startswith('death.attack.') else 'ic2.' + k] = v.replace('%d', '%s')

def name(code, key, fallback):
    v = LANG[code].get(key) or LANG['en_us'].get(key) or LANG[code].get('ic2.' + key) or LANG['en_us'].get('ic2.' + key)
    if not v:
        warnings.append(f'no name for {key}')
        v = fallback
    return v

LEGACY = {}   # legacy key -> modern id
ITEM_TAGS = {}

def add_tag(tags, tag, entry):
    tags.setdefault(tag, [])
    if entry not in tags[tag]: tags[tag].append(entry)

# ---------- simple items
simple_ids = []
for it in content.SIMPLE_ITEMS:
    iid = it['id']
    simple_ids.append(iid)
    for leg in it['legacy']: LEGACY[leg] = 'ic2:' + iid
    copy(os.path.join(OTEX, 'items', it['tex']), os.path.join(A, 'textures/item', iid + '.png'))
    write(os.path.join(A, 'models/item', iid + '.json'), {'parent': 'minecraft:item/generated', 'textures': {'layer0': 'ic2:item/' + iid}})
    for code in out_lang: out_lang[code]['item.ic2.' + iid] = name(code, it['lang'], iid)
    for t in it['tags']: add_tag(ITEM_TAGS, t, 'ic2:' + iid)
write(os.path.join(RES, 'ic2/content/simple_items.txt'), '\n'.join(simple_ids) + '\n')

# ---------- special items
def gen_item(iid, tex, lang, legacy=()):
    for leg in legacy: LEGACY[leg] = 'ic2:' + iid
    copy(os.path.join(OTEX, 'items', tex), os.path.join(A, 'textures/item', iid + '.png'))
    write(os.path.join(A, 'models/item', iid + '.json'), {'parent': 'minecraft:item/handheld' if 'Tool' in tex else 'minecraft:item/generated', 'textures': {'layer0': 'ic2:item/' + iid}})
    for code in out_lang: out_lang[code]['item.ic2.' + iid] = name(code, lang, iid)

pass
gen_item('wrench', 'itemToolWrench.png', 'itemToolWrench', ['itemToolWrench'])
for iid, tex, lang, legacy in [('empty_cell', 'cell/itemCellEmpty.png', 'itemCellEmpty', ['itemCellEmpty', 'itemCellEmpty@0']), ('water_cell', 'cell/itemCellWater.png', 'itemCellWater', ['itemCellEmpty@1', 'itemCellWater']),
        ('lava_cell', 'cell/itemCellLava.png', 'itemCellLava', ['itemCellEmpty@2', 'itemCellLava']), ('air_cell', 'cell/itemCellAir.png', 'itemCellAir', ['itemCellEmpty@5', 'itemCellAir']),
        ('iron_block_cutting_blade', 'resources/itemIronBlockCuttingBlade.png', 'itemIronBlockCuttingBlade', ['itemIronBlockCuttingBlade', 'itemBlockCuttingBlade@0']),
        ('refined_iron_block_cutting_blade', 'resources/itemAdvIronBlockCuttingBlade.png', 'itemAdvIronBlockCuttingBlade', ['itemAdvIronBlockCuttingBlade', 'itemBlockCuttingBlade@1']),
        ('diamond_block_cutting_blade', 'resources/itemDiamondBlockCuttingBlade.png', 'itemDiamondBlockCuttingBlade', ['itemDiamondBlockCuttingBlade', 'itemBlockCuttingBlade@2'])]:
    gen_item(iid, tex, lang, legacy)
for iid, leg in [('mining_drill', 'itemToolDrill'), ('diamond_drill', 'itemToolDDrill'), ('iridium_drill', 'itemToolIridiumDrill'), ('chainsaw', 'itemToolChainsaw'),
        ('electric_hoe', 'itemToolHoe'), ('electric_treetap', 'itemTreetapElectric'), ('electric_wrench', 'itemToolWrenchElectric'),
        ('bronze_pickaxe', 'itemToolBronzePickaxe'), ('bronze_axe', 'itemToolBronzeAxe'), ('bronze_sword', 'itemToolBronzeSword'), ('bronze_shovel', 'itemToolBronzeSpade'), ('bronze_hoe', 'itemToolBronzeHoe'),
        ('bronze_helmet', 'itemArmorBronzeHelmet'), ('bronze_chestplate', 'itemArmorBronzeChestplate'), ('bronze_leggings', 'itemArmorBronzeLegs'), ('bronze_boots', 'itemArmorBronzeBoots'),
        ('composite_vest', 'itemArmorAlloyChestplate'), ('nano_helmet', 'itemArmorNanoHelmet'), ('nano_chestplate', 'itemArmorNanoChestplate'), ('nano_leggings', 'itemArmorNanoLegs'), ('nano_boots', 'itemArmorNanoBoots'),
        ('quantum_helmet', 'itemArmorQuantumHelmet'), ('quantum_chestplate', 'itemArmorQuantumChestplate'), ('quantum_leggings', 'itemArmorQuantumLegs'), ('quantum_boots', 'itemArmorQuantumBoots'),
        ('electric_jetpack', 'itemArmorJetpackElectric'), ('batpack', 'itemArmorBatpack'), ('advanced_batpack', 'itemArmorAdvBatpack'), ('energy_pack', 'itemArmorEnergypack'), ('lappack', 'itemArmorEnergypack'),
        ('hazmat_helmet', 'itemArmorHazmatHelmet'), ('hazmat_chestplate', 'itemArmorHazmatChestplate'), ('hazmat_leggings', 'itemArmorHazmatLeggings'), ('rubber_boots', 'itemArmorRubBoots'),
        ('nightvision_goggles', 'itemNightvisionGoggles'), ('solar_helmet', 'itemSolarHelmet'), ('static_boots', 'itemStaticBoots')]:
    tex = leg + '.png'
    if iid == 'lappack': tex = 'itemArmorLappack.png'
    gen_item(iid, tex, leg, [leg])
LEGACY['itemArmorLappack'] = 'ic2:lappack'
for code in out_lang:
    out_lang[code]['item.ic2.lappack'] = name(code, 'itemArmorLappack', 'Lappack')
# nano saber: off / active textures switched by the "active" flag
copy(os.path.join(OTEX, 'items/itemNanoSaber.off.png'), os.path.join(A, 'textures/item/nano_saber.png'))
copy(os.path.join(OTEX, 'items/itemNanoSaber.active.png'), os.path.join(A, 'textures/item/nano_saber_active.png'))
write(os.path.join(A, 'models/item/nano_saber_active.json'), {'parent': 'minecraft:item/handheld', 'textures': {'layer0': 'ic2:item/nano_saber_active'}})
write(os.path.join(A, 'models/item/nano_saber.json'), {'parent': 'minecraft:item/handheld', 'textures': {'layer0': 'ic2:item/nano_saber'}, 'overrides': [{'predicate': {'ic2:active': 1}, 'model': 'ic2:item/nano_saber_active'}]})
LEGACY['itemNanoSaber'] = 'ic2:nano_saber'
for code in out_lang: out_lang[code]['item.ic2.nano_saber'] = name(code, 'itemNanoSaber', 'Nano Saber')
for f in os.listdir(os.path.join(OTEX, 'armor')):
    base, num = f[:-4].rsplit('_', 1)
    copy(os.path.join(OTEX, 'armor', f), os.path.join(A, 'textures/models/armor', f'{base}_layer_{num}.png'))
# fluids, fluid blocks and cells
FLUIDS = [('uu_matter', 'uumatter', 'fluidUuMatter', 'itemCellUuMatter', ['itemCellEmpty@3', 'itemCellUuMatter']), ('construction_foam', 'constructionfoam', 'fluidConstructionFoam', 'itemCellCF', ['itemCellEmpty@4', 'itemCellCF']),
          ('coolant', 'coolant', 'fluidCoolant', 'itemCellCoolant', ['itemCellEmpty@9', 'itemCellCoolant']), ('hot_coolant', 'hotcoolant', 'fluidHotCoolant', 'itemCellHotCoolant', ['itemCellEmpty@10', 'itemCellHotCoolant']),
          ('pahoehoe_lava', 'pahoehoelava', 'fluidPahoehoeLava', 'itemCellPahoehoelava', ['itemCellEmpty@11', 'itemCellPahoehoelava']), ('biomass', 'biomass', 'fluidBiomass', 'itemCellBiomass', ['itemCellEmpty@6', 'itemCellBiomass']),
          ('biogas', 'biogas', 'fluidBiogas', 'itemCellBiogas', ['itemCellEmpty@7', 'itemCellBiogas']), ('distilled_water', 'distilledwater', 'fluidDistilledWater', 'itemCellDistilledWater', ['itemCellEmpty@12', 'itemCellDistilledWater']),
          ('superheated_steam', 'superheatedsteam', 'fluidSuperheatedSteam', 'itemCellSuperheatedSteam', ['itemCellEmpty@13', 'itemCellSuperheatedSteam']), ('steam', 'steam', 'fluidSteam', 'itemCellSteam', ['itemCellEmpty@14', 'itemCellSteam']),
          ('hot_water', 'hotwater', 'fluidHotWater', None, [])]
GASEOUS = {'biogas', 'superheated_steam', 'steam'}
for fid, tex, lang, cell, legacy in FLUIDS:
    for kind in ('still', 'flow'):
        src = os.path.join(OTEX, 'blocks/fluids', f'{tex}_{kind}.png')
        if os.path.exists(src): copy(src, os.path.join(A, 'textures/block/fluid', f'{tex}_{kind}.png'))
    for code in out_lang: out_lang[code]['fluid.ic2.' + fid] = name(code, lang, fid)
    if fid not in GASEOUS:
        write(os.path.join(A, 'models/block/fluid', fid + '.json'), {'textures': {'particle': f'ic2:block/fluid/{tex}_still'}})
        write(os.path.join(A, 'blockstates', fid + '.json'), {'variants': {'': {'model': 'ic2:block/fluid/' + fid}}})
        for code in out_lang: out_lang[code]['block.ic2.' + fid] = name(code, lang, fid)
    if cell:
        gen_item(fid + '_cell', 'cell/' + cell + '.png', cell, legacy)
    else:
        gen_item(fid + '_cell', 'cell/itemCellWater.png', lang, [])
gen_item('universal_fluid_cell', 'cell/itemFluidCell.png', 'itemFluidCell', ['itemFluidCell'])
for iid, leg, model in [('wood_rotor', 'itemwoodrotor', 'rotorWoodmodel'), ('iron_rotor', 'itemironrotor', 'rotorIronmodel'), ('steel_rotor', 'itemsteelrotor', 'rotorSteelmodel'), ('carbon_rotor', 'itemwcarbonrotor', 'rotorCarbonmodel')]:
    gen_item(iid, 'rotors/' + leg + '.png', leg, [leg])
    copy(os.path.join(OTEX, 'items/rotors', model + '.png'), os.path.join(A, 'textures/item/rotor', iid.replace('_rotor', '').join(['rotor_', '_model']) + '.png'))
gen_item('filled_tin_can', 'itemTinCanFilled.png', 'itemTinCanFilled', ['itemTinCanFilled'])
gen_item('frequency_transmitter', 'itemFreq.png', 'itemFreq', ['itemFreq'])
gen_item('mining_laser', 'itemToolMiningLaser.png', 'itemToolMiningLaser', ['itemToolMiningLaser'])
gen_item('tool_box', 'itemToolbox.png', 'itemToolbox', ['itemToolbox'])
gen_item('eu_meter', 'itemToolMEter.png', 'itemToolMEter', ['itemToolMEter'])
gen_item('wind_meter', 'windmeter.png', 'windmeter', ['windmeter'])
for dye, leg in (('black', 'Black'), ('red', 'Red'), ('green', 'Green'), ('brown', 'Brown'), ('blue', 'Blue'), ('purple', 'Purple'), ('cyan', 'Cyan'), ('light_gray', 'LightGrey'),
                 ('gray', 'DarkGrey'), ('pink', 'Pink'), ('lime', 'Lime'), ('yellow', 'Yellow'), ('light_blue', 'Cloud'), ('magenta', 'Magenta'), ('orange', 'Orange'), ('white', 'White')):
    gen_item(dye + '_painter', 'itemToolPainter' + leg + '.png', 'itemToolPainter' + leg, ['itemToolPainter' + leg])
for iid, leg in [('uranium_fuel_rod', 'reactorUraniumSimple'), ('dual_uranium_fuel_rod', 'reactorUraniumDual'), ('quad_uranium_fuel_rod', 'reactorUraniumQuad'),
        ('mox_fuel_rod', 'reactorMOXSimple'), ('dual_mox_fuel_rod', 'reactorMOXDual'), ('quad_mox_fuel_rod', 'reactorMOXQuad'), ('lithium_fuel_rod', 'reactorLithiumCell'),
        ('heat_storage', 'reactorCoolantSimple'), ('tri_heat_storage', 'reactorCoolantTriple'), ('hex_heat_storage', 'reactorCoolantSix'), ('plating', 'reactorPlating'),
        ('heat_plating', 'reactorPlatingHeat'), ('containment_plating', 'reactorPlatingExplosive'), ('heat_exchanger', 'reactorHeatSwitch'), ('reactor_heat_exchanger', 'reactorHeatSwitchCore'),
        ('component_heat_exchanger', 'reactorHeatSwitchSpread'), ('advanced_heat_exchanger', 'reactorHeatSwitchDiamond'), ('heat_vent', 'reactorVent'), ('reactor_heat_vent', 'reactorVentCore'),
        ('overclocked_heat_vent', 'reactorVentGold'), ('component_heat_vent', 'reactorVentSpread'), ('advanced_heat_vent', 'reactorVentDiamond'), ('neutron_reflector', 'reactorReflector'),
        ('thick_neutron_reflector', 'reactorReflectorThick'), ('rsh_condensator', 'reactorCondensator'), ('lzh_condensator', 'reactorCondensatorLap')]:
    gen_item(iid, leg + '.png', leg, [leg, leg + '@1', leg + '@*'])
gen_item('crystal_memory', 'resources/itemcrystalmemory.png', 'itemcrystalmemory', ['itemcrystalmemory'])
for kind, leg in (('chilling', 'Chilling'), ('cultivation', 'Cultivation'), ('desertification', 'Desertification'), ('flatification', 'Flatification'), ('irrigation', 'Irrigation'), ('mushroom', 'Mushroom')):
    gen_item(kind + '_terraformer_blueprint', 'itemTFBP' + leg + '.png', 'itemTFBP' + leg, ['itemTFBP' + leg])
gen_item('foam_sprayer', 'itemFoamSprayer.png', 'itemFoamSprayer', ['itemFoamSprayer'])
gen_item('cf_pack', 'itemArmorCFPack.png', 'itemArmorCFPack', ['itemArmorCFPack'])
gen_item('dynamite', 'itemDynamite.png', 'itemDynamite', ['itemDynamite'])
gen_item('sticky_dynamite', 'itemDynamiteSticky.png', 'itemDynamiteSticky', ['itemDynamiteSticky'])
gen_item('dynamite_remote', 'itemRemote.png', 'itemRemote', ['itemRemote'])
# ---------- mugs, coffee, booze barrel
gen_item('empty_mug', 'itemMugEmpty.png', 'itemMugEmpty', ['itemMugEmpty'])
for meta, iid in enumerate(['cold_coffee', 'dark_coffee', 'coffee']):
    gen_item(iid, f'itemMugCoffee_{meta}.png', f'itemMugCoffee_{meta}', [f'itemMugCoffee@{meta}'] + (['itemMugCoffee'] if meta == 0 else []))
gen_item('barrel', 'itemBarrel.png', 'item.EmptyBoozeBarrel', ['itemBarrel'])
LEGACY['itemMugBooze'] = 'ic2:booze_mug'
BOOZE = ['beer.Brew', 'beer.Youngster', 'beer.Beer', 'beer.Ale', 'beer.Dragonblood', 'beer.Black Stuff', 'rum']
booze_model = {'parent': 'minecraft:item/generated', 'textures': {'layer0': 'ic2:item/booze/beer_brew'}, 'overrides': []}
for n, tex in enumerate(BOOZE):
    bname = tex.replace('beer.', 'beer_').replace(' ', '_').lower()
    copy(os.path.join(OTEX, 'items', f'ic2.itemMugBooze.{tex}.png'), os.path.join(A, 'textures/item/booze', bname + '.png'))
    write(os.path.join(A, 'models/item/booze', bname + '.json'), {'parent': 'minecraft:item/generated', 'textures': {'layer0': 'ic2:item/booze/' + bname}})
    booze_model['overrides'].append({'predicate': {'ic2:booze': n / 10}, 'model': 'ic2:item/booze/' + bname})
write(os.path.join(A, 'models/item/booze_mug.json'), booze_model)
for code in out_lang: out_lang[code]['item.ic2.booze_mug'] = 'Booze'
write(os.path.join(D, 'ic2/recipe/dark_coffee_smelting.json'), {'type': 'minecraft:smelting', 'category': 'food', 'ingredient': {'item': 'ic2:cold_coffee'}, 'result': {'id': 'ic2:dark_coffee'}, 'experience': 0.1, 'cookingtime': 200})
for bid, tex in (('dynamite', 'blockDynamite'), ('dynamite_remote', 'blockDynamiteRemote')):
    copy(os.path.join(OTEX, 'blocks', tex + '.png'), os.path.join(A, 'textures/block', bid + '.png'))
    write(os.path.join(A, 'models/block', bid + '.json'), {'parent': 'minecraft:block/template_torch', 'render_type': 'minecraft:cutout', 'textures': {'torch': 'ic2:block/' + bid}})
    write(os.path.join(A, 'models/block', bid + '_wall.json'), {'parent': 'minecraft:block/template_torch_wall', 'render_type': 'minecraft:cutout', 'textures': {'torch': 'ic2:block/' + bid}})
    write(os.path.join(A, 'blockstates', bid + '.json'), {'variants': {'facing=up': {'model': 'ic2:block/' + bid}, 'facing=east': {'model': f'ic2:block/{bid}_wall'},
        'facing=south': {'model': f'ic2:block/{bid}_wall', 'y': 90}, 'facing=west': {'model': f'ic2:block/{bid}_wall', 'y': 180}, 'facing=north': {'model': f'ic2:block/{bid}_wall', 'y': 270}}})
    for code in out_lang: out_lang[code]['block.ic2.' + bid] = name(code, 'itemDynamite', 'Dynamite')
for code in out_lang: out_lang[code]['entity.ic2.dynamite'] = name(code, 'itemDynamite', 'Dynamite')
gen_item('od_scanner', 'itemScanner.png', 'itemScanner', ['itemScanner'])
gen_item('ov_scanner', 'itemScannerAdv.png', 'itemScannerAdv', ['itemScannerAdv'])
gen_item('scrap_box', 'itemScrapbox.png', 'itemScrapbox', ['itemScrapbox'])
gen_item('forge_hammer', 'itemToolForgeHammer.png', 'itemToolForgeHammer', ['itemToolForgeHammer'])
gen_item('cutter', 'itemToolCutter.png', 'itemToolCutter', ['itemToolCutter'])
gen_item('single_use_battery', 'itemBatSU.png', 'itemBatSU', ['itemBatSU'])
for iid, leg in [('re_battery', 'itemBatRE'), ('advanced_re_battery', 'itemAdvBat'), ('energy_crystal', 'itemBatCrystal'), ('lapotron_crystal', 'itemBatLamaCrystal'), ('charging_re_battery', 'itemBatChargeRE'), ('charging_advanced_battery', 'itemBatChargeAdv'), ('charging_energy_crystal', 'itemBatChargeCrystal'), ('charging_lapotron_crystal', 'itemBatChargeLamaCrystal')]:
    LEGACY[leg] = 'ic2:' + iid
    overrides = []
    for i in range(5):
        copy(os.path.join(OTEX, 'items', f'{leg}.{i}.png'), os.path.join(A, 'textures/item', f'{iid}_{i}.png'))
        write(os.path.join(A, 'models/item', f'{iid}_{i}.json'), {'parent': 'minecraft:item/generated', 'textures': {'layer0': f'ic2:item/{iid}_{i}'}})
        if i: overrides.append({'predicate': {'ic2:charge_level': i / 4 - 0.01}, 'model': f'ic2:item/{iid}_{i}'})
    write(os.path.join(A, 'models/item', iid + '.json'), {'parent': 'minecraft:item/generated', 'textures': {'layer0': f'ic2:item/{iid}_0'}, 'overrides': overrides})
    for code in out_lang: out_lang[code]['item.ic2.' + iid] = name(code, leg, iid)
LEGACY['itemBatREDischarged'] = 'ic2:re_battery'
LEGACY.update({'itemIngot@0': 'minecraft:copper_ingot', 'itemIngotCopper': 'minecraft:copper_ingot', 'blockMetal@0': 'minecraft:copper_block', 'blockMetal': 'minecraft:copper_block',
               'blockOreCopper': 'minecraft:copper_ore'})
for meta, (iid, leg) in enumerate([('overclocker_upgrade', 'overclockerUpgrade'), ('transformer_upgrade', 'transformerUpgrade'), ('energy_storage_upgrade', 'energyStorageUpgrade'),
        ('ejector_upgrade', 'ejectorUpgrade'), ('fluid_ejector_upgrade', 'fluidEjectorUpgrade'), ('redstone_inverter_upgrade', 'redstoneinvUpgrade'), ('pulling_upgrade', 'pullingUpgrade')]):
    LEGACY[f'upgradeModule@{meta}'] = 'ic2:' + iid
    copy(os.path.join(OTEX, 'items/upgrade', leg + '.png'), os.path.join(A, 'textures/item', iid + '.png'))
    model = {'parent': 'minecraft:item/generated', 'textures': {'layer0': 'ic2:item/' + iid}}
    if os.path.exists(os.path.join(OTEX, 'items/upgrade', leg + '.0.png')):
        model['overrides'] = []
        for d in range(6):
            copy(os.path.join(OTEX, 'items/upgrade', f'{leg}.{d}.png'), os.path.join(A, 'textures/item', f'{iid}_{d + 1}.png'))
            write(os.path.join(A, 'models/item', f'{iid}_{d + 1}.json'), {'parent': 'minecraft:item/generated', 'textures': {'layer0': f'ic2:item/{iid}_{d + 1}'}})
            model['overrides'].append({'predicate': {'ic2:dir': (d + 1) / 6 - 0.01}, 'model': f'ic2:item/{iid}_{d + 1}'})
    write(os.path.join(A, 'models/item', iid + '.json'), model)
    for code in out_lang: out_lang[code]['item.ic2.' + iid] = name(code, leg, iid)

# ---------- blocks
SIDE_MAP = [[3, 5, 1, 0, 4, 2], [5, 3, 1, 0, 2, 4], [0, 1, 3, 5, 4, 2], [0, 1, 5, 3, 2, 4], [0, 1, 2, 4, 3, 5], [0, 1, 4, 2, 5, 3]]
FACES = ['down', 'up', 'north', 'south', 'west', 'east']
BLOCK_TAGS = {}
for b in content.BLOCKS:
    bid = b['id']
    for leg in b['legacy']: LEGACY[leg] = 'ic2:' + bid
    for code in out_lang: out_lang[code]['block.ic2.' + bid] = name(code, b['lang'], bid)
    for t in b.get('tags', []): add_tag(BLOCK_TAGS, t, 'ic2:' + bid)
    for t in b.get('item_tags', []): add_tag(ITEM_TAGS, t, 'ic2:' + bid)
    src = os.path.join(OTEX, 'blocks', b['tex'])
    img = Image.open(src).convert('RGBA')
    size = img.height
    count = img.width // size
    tiles = []
    for i in range(12):
        idx = i % count if count in (1, 6, 12) else (i // 6 if count == 2 else 0)
        tiles.append(img.crop((idx * size, 0, idx * size + size, size)))
    unique = {}
    names = []
    for i, t in enumerate(tiles):
        key = t.tobytes()
        if key not in unique:
            unique[key] = f'{bid}_{len(unique)}' if count > 1 else bid
            path = os.path.join(A, 'textures/block', unique[key] + '.png')
            os.makedirs(os.path.dirname(path), exist_ok=True)
            t.save(path)
        names.append('ic2:block/' + unique[key])
    if b['kind'] == 'cube':
        write(os.path.join(A, 'models/block', bid + '.json'), {'parent': 'minecraft:block/cube', 'textures': dict(zip(FACES, names[:6])) | {'particle': names[2]}} if bid in ('industrial_tnt', 'nuke') else {'parent': 'minecraft:block/cube_all', 'textures': {'all': names[0]}})
        write(os.path.join(A, 'blockstates', bid + '.json'), {'variants': {'': {'model': 'ic2:block/' + bid}}})
        write(os.path.join(A, 'models/item', bid + '.json'), {'parent': 'ic2:block/' + bid})
    elif b['kind'] == 'safe':
        write(os.path.join(A, 'models/block', bid + '.json'), {'textures': {'particle': names[2]}})
        write(os.path.join(A, 'blockstates', bid + '.json'), {'variants': {'': {'model': 'ic2:block/' + bid}}})
        write(os.path.join(A, 'models/item', bid + '.json'), {'parent': 'minecraft:item/chest', 'textures': {'particle': names[2]}})
    else:
        variants = {}
        for facing in range(6):
            for active in (0, 1):
                tex = {FACES[side]: names[SIDE_MAP[facing][side] + 6 * active] for side in range(6)}
                tex['particle'] = tex['north']
                mname = f'{bid}_{FACES[facing]}' + ('_active' if active else '')
                write(os.path.join(A, 'models/block', mname + '.json'), {'parent': 'ic2:block/chargepad_base' if bid.startswith('chargepad') else 'minecraft:block/cube', 'textures': tex})
                variants[f'active={"true" if active else "false"},facing={FACES[facing]}'] = {'model': 'ic2:block/' + mname}
        write(os.path.join(A, 'blockstates', bid + '.json'), {'variants': variants})
        write(os.path.join(A, 'models/item', bid + '.json'), {'parent': f'ic2:block/{bid}_south'})
    drop = b.get('drop', 'self')
    drop_item = 'ic2:' + bid if drop == 'self' else 'ic2:' + drop
    write(os.path.join(D, 'ic2/loot_table/blocks', bid + '.json'), {'type': 'minecraft:block', 'pools': [{'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': drop_item}], 'conditions': [{'condition': 'minecraft:survives_explosion'}]}]})

write(os.path.join(A, 'models/block/chargepad_base.json'), {'parent': 'minecraft:block/block', 'elements': [{'from': [0, 0, 0], 'to': [16, 15.2, 16], 'faces': {
    f: {'texture': '#' + f, **({'cullface': f} if f != 'up' else {}), **({'uv': [0, 0.8, 16, 16]} if f not in ('up', 'down') else {})} for f in FACES}}]})
# Industrial TNT ignites when broken; it must never also return an intact charge.
write(os.path.join(D, 'ic2/loot_table/blocks/industrial_tnt.json'), {'type': 'minecraft:block', 'pools': []})
# booze barrel block: wooden barrel on a scaffold, the tap texture on the side holding a treetap
img = Image.open(os.path.join(OTEX, 'blocks/blockBarrel.png')).convert('RGBA')
for i in range(6):
    path = os.path.join(A, 'textures/block/barrel', f'{i}.png'); os.makedirs(os.path.dirname(path), exist_ok=True); img.crop((i * 16, 0, i * 16 + 16, 16)).save(path)
copy(os.path.join(OTEX, 'blocks/blockBarrel.tap.png'), os.path.join(A, 'textures/block/barrel/tap.png'))
variants = {}
for tap in ['none', 'north', 'south', 'west', 'east']:
    tex = {FACES[side]: f'ic2:block/barrel/{SIDE_MAP[3][side]}' for side in range(6)}
    if tap != 'none': tex[tap] = 'ic2:block/barrel/tap'
    tex['particle'] = 'ic2:block/barrel/2'
    write(os.path.join(A, 'models/block', f'barrel_{tap}.json'), {'parent': 'minecraft:block/cube', 'textures': tex})
    variants[f'tap={tap}'] = {'model': f'ic2:block/barrel_{tap}'}
write(os.path.join(A, 'blockstates/barrel.json'), {'variants': variants})
for code in out_lang: out_lang[code]['block.ic2.barrel'] = name(code, 'item.LBoozeBarrel', 'Booze Barrel')
add_tag(BLOCK_TAGS, 'minecraft:mineable/axe', 'ic2:barrel')
# a barrel broken as a block leaves the scaffold and an empty barrel; left-clicking it takes the filled barrel off instead
write(os.path.join(D, 'ic2/loot_table/blocks/barrel.json'), {'type': 'minecraft:block', 'pools': [{'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': 'ic2:scaffold'}]}, {'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': 'ic2:barrel'}]}]})

# ---------- cables
COLORS = ['black', 'red', 'green', 'brown', 'blue', 'purple', 'cyan', 'lightGray', 'gray', 'pink', 'lime', 'yellow', 'lightBlue', 'magenta', 'orange', 'white']
CABLES = [('insulated_copper', 0, 'Cable', 6, True), ('copper', 1, 'CableO', 4, False), ('gold', 2, 'GoldCable', 3, False), ('insulated_gold', 3, 'GoldCableI', 6, True),
          ('iron', 5, 'IronCable', 6, False), ('insulated_iron', 6, 'IronCableI', 10, True), ('glass_fibre', 9, 'GlassCable', 4, True), ('tin', 10, 'TinCable', 4, False),
          ('detector', 11, 'DetectorCable', 8, False), ('splitter', 12, 'SplitterCable', 8, False), ('insulated_tin', 13, 'TinCableI', 6, True)]
CTEX = os.path.join(OTEX, 'blocks/wiring/cable')
DIRS = {'north': (2, 0), 'south': (2, 1), 'west': (0, 0), 'east': (0, 1), 'down': (1, 0), 'up': (1, 1)}
for name_, meta, tex, th, colorable in CABLES:
    cid = name_ + '_cable'
    LEGACY[f'itemCable@{meta}'] = 'ic2:' + cid
    if meta == 0: LEGACY['itemCable'] = 'ic2:' + cid
    for code in out_lang: out_lang[code]['block.ic2.' + cid] = name(code, 'item' + tex, cid)
    add_tag(BLOCK_TAGS, 'minecraft:mineable/axe', 'ic2:' + cid)
    copy(os.path.join(OTEX, 'items', 'item' + tex + '.png'), os.path.join(A, 'textures/item', cid + '.png'))
    write(os.path.join(A, 'models/item', cid + '.json'), {'parent': 'minecraft:item/generated', 'textures': {'layer0': 'ic2:item/' + cid}})
    # TileEntityCable.color of the original is the dye index (1 red .. 15 white); 0 is the plain (black) insulation
    variants = [('', 0)] + ([('.' + c, i) for i, c in enumerate(COLORS) if i > 0] if colorable else [])
    sp, e = (16 - th) / 2, (16 + th) / 2
    multipart = []
    for suffix, color in variants:
        src = os.path.join(CTEX, 'block' + tex + suffix + '.png')
        if not os.path.exists(src): src = os.path.join(CTEX, 'block' + tex + '.png')
        img = Image.open(src).convert('RGBA')
        states = [(False, img.crop((0, 0, img.height, img.height)))]
        if img.width >= 2 * img.height: states.append((True, img.crop((img.height, 0, 2 * img.height, img.height))))
        for active, tile in states:
            tname = f'{cid}_{color}' + ('_active' if active else '')
            path = os.path.join(A, 'textures/block/cable', tname + '.png'); os.makedirs(os.path.dirname(path), exist_ok=True); tile.save(path)
            texref = 'ic2:block/cable/' + tname
            faces = {f: {'texture': '#t'} for f in FACES}
            write(os.path.join(A, 'models/block/cable', tname + '_core.json'), {'textures': {'t': texref, 'particle': texref}, 'elements': [{'from': [sp, sp, sp], 'to': [e, e, e], 'faces': faces}]})
            when = {'color': str(color)}
            if len(states) > 1: when['active'] = 'true' if active else 'false'
            multipart.append({'when': dict(when), 'apply': {'model': f'ic2:block/cable/{tname}_core'}})
            for d, (axis, pos) in DIRS.items():
                fr, to = [sp, sp, sp], [e, e, e]
                if pos: to[axis] = 16
                else: fr[axis] = 0
                if pos: fr[axis] = e
                else: to[axis] = sp
                arm_faces = {f: {'texture': '#t'} for f in FACES if not ((f in ('west', 'east') and axis == 0) or (f in ('down', 'up') and axis == 1) or (f in ('north', 'south') and axis == 2)) or f == d}
                write(os.path.join(A, 'models/block/cable', f'{tname}_{d}.json'), {'textures': {'t': texref, 'particle': texref}, 'elements': [{'from': fr, 'to': to, 'faces': arm_faces}]})
                w = dict(when); w[d] = 'true'
                multipart.append({'when': w, 'apply': {'model': f'ic2:block/cable/{tname}_{d}'}})
    write(os.path.join(A, 'blockstates', cid + '.json'), {'multipart': multipart})
    write(os.path.join(D, 'ic2/loot_table/blocks', cid + '.json'), {'type': 'minecraft:block', 'pools': [{'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': 'ic2:' + cid}], 'conditions': [{'condition': 'minecraft:survives_explosion'}]}]})

# ---------- rubber tree
for code in out_lang:
    out_lang[code]['block.ic2.rubber_wood'] = name(code, 'blockRubWood', 'Rubber Wood')
    out_lang[code]['block.ic2.rubber_leaves'] = name(code, 'blockRubLeaves', 'Rubber Leaves')
    out_lang[code]['block.ic2.rubber_sapling'] = name(code, 'blockRubSapling', 'Rubber Sapling')
    out_lang[code]['item.ic2.treetap'] = name(code, 'itemTreetap', 'Treetap')
LEGACY.update({'blockRubWood': 'ic2:rubber_wood', 'blockRubLeaves': 'ic2:rubber_leaves', 'blockRubSapling': 'ic2:rubber_sapling', 'itemTreetap': 'ic2:treetap'})
gen_item('treetap', 'itemTreetap.png', 'itemTreetap')
variants = {}
for hole, suffix in [('none', ''), ('wet', '.wet'), ('dry', '.dry')]:
    img = Image.open(os.path.join(OTEX, 'blocks', 'blockRubWood' + suffix + '.png')).convert('RGBA')
    tiles = [img.crop((i * img.height, 0, (i + 1) * img.height, img.height)) for i in range(6)]
    for i, tile in enumerate(tiles):
        path = os.path.join(A, 'textures/block/rubber_wood', f'{hole}_{i}.png'); os.makedirs(os.path.dirname(path), exist_ok=True); tile.save(path)
    for facing in range(2, 6):
        tex = {FACES[side]: f'ic2:block/rubber_wood/{hole}_{SIDE_MAP[facing][side]}' for side in range(6)}
        tex['particle'] = tex['north']
        mname = f'rubber_wood_{hole}_{FACES[facing]}'
        write(os.path.join(A, 'models/block', mname + '.json'), {'parent': 'minecraft:block/cube', 'textures': tex})
        variants[f'facing={FACES[facing]},hole={hole}'] = {'model': 'ic2:block/' + (mname if hole != 'none' else 'rubber_wood_none_north')}
write(os.path.join(A, 'blockstates/rubber_wood.json'), {'variants': variants})
write(os.path.join(A, 'models/item/rubber_wood.json'), {'parent': 'ic2:block/rubber_wood_none_north'})
copy(os.path.join(OTEX, 'blocks/blockRubLeaves.png'), os.path.join(A, 'textures/block/rubber_leaves.png'))
write(os.path.join(A, 'models/block/rubber_leaves.json'), {'parent': 'minecraft:block/leaves', 'textures': {'all': 'ic2:block/rubber_leaves'}})
write(os.path.join(A, 'blockstates/rubber_leaves.json'), {'variants': {'': {'model': 'ic2:block/rubber_leaves'}}})
write(os.path.join(A, 'models/item/rubber_leaves.json'), {'parent': 'ic2:block/rubber_leaves'})
copy(os.path.join(OTEX, 'blocks/blockRubSapling.png'), os.path.join(A, 'textures/block/rubber_sapling.png'))
write(os.path.join(A, 'models/block/rubber_sapling.json'), {'parent': 'minecraft:block/cross', 'render_type': 'minecraft:cutout', 'textures': {'cross': 'ic2:block/rubber_sapling'}})
write(os.path.join(A, 'blockstates/rubber_sapling.json'), {'variants': {'': {'model': 'ic2:block/rubber_sapling'}}})
write(os.path.join(A, 'models/item/rubber_sapling.json'), {'parent': 'minecraft:item/generated', 'textures': {'layer0': 'ic2:block/rubber_sapling'}})
def hole_pool(h):
    return {'rolls': 1, 'conditions': [{'condition': 'minecraft:block_state_property', 'block': 'ic2:rubber_wood', 'properties': {'hole': h}}, {'condition': 'minecraft:random_chance', 'chance': 1 / 6}],
            'entries': [{'type': 'minecraft:item', 'name': 'ic2:resin'}]}
write(os.path.join(D, 'ic2/loot_table/blocks/rubber_wood.json'), {'type': 'minecraft:block', 'pools': [{'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': 'ic2:rubber_wood'}], 'conditions': [{'condition': 'minecraft:survives_explosion'}]}, hole_pool('wet'), hole_pool('dry')]})
shears = {'condition': 'minecraft:match_tool', 'predicate': {'items': 'minecraft:shears'}}
write(os.path.join(D, 'ic2/loot_table/blocks/rubber_leaves.json'), {'type': 'minecraft:block', 'pools': [{'rolls': 1, 'entries': [{'type': 'minecraft:alternatives', 'children': [
    {'type': 'minecraft:item', 'name': 'ic2:rubber_leaves', 'conditions': [shears]},
    {'type': 'minecraft:item', 'name': 'ic2:rubber_sapling', 'conditions': [{'condition': 'minecraft:survives_explosion'}, {'condition': 'minecraft:random_chance', 'chance': 1 / 35}]}]}]}]})
write(os.path.join(D, 'ic2/loot_table/blocks/rubber_sapling.json'), {'type': 'minecraft:block', 'pools': [{'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': 'ic2:rubber_sapling'}], 'conditions': [{'condition': 'minecraft:survives_explosion'}]}]})
# iron fence: vanilla fence models with the IC2 texture; the magnet level does not change the look
copy(os.path.join(OTEX, 'blocks/blockFenceIron.png'), os.path.join(A, 'textures/block/iron_fence.png'))
write(os.path.join(A, 'models/block/iron_fence_post.json'), {'parent': 'minecraft:block/fence_post', 'textures': {'texture': 'ic2:block/iron_fence'}})
write(os.path.join(A, 'models/block/iron_fence_side.json'), {'parent': 'minecraft:block/fence_side', 'textures': {'texture': 'ic2:block/iron_fence'}})
write(os.path.join(A, 'models/item/iron_fence.json'), {'parent': 'minecraft:block/fence_inventory', 'textures': {'texture': 'ic2:block/iron_fence'}})
write(os.path.join(A, 'blockstates/iron_fence.json'), {'multipart': [{'apply': {'model': 'ic2:block/iron_fence_post'}}] + [
    {'when': {d: 'true'}, 'apply': {'model': 'ic2:block/iron_fence_side', 'y': r, 'uvlock': True}} for d, r in (('north', 0), ('east', 90), ('south', 180), ('west', 270))]})
write(os.path.join(D, 'ic2/loot_table/blocks/iron_fence.json'), {'type': 'minecraft:block', 'pools': [{'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': 'ic2:iron_fence'}], 'conditions': [{'condition': 'minecraft:survives_explosion'}]}]})
for t in ('minecraft:fences', 'minecraft:mineable/pickaxe'): add_tag(BLOCK_TAGS, t, 'ic2:iron_fence')
add_tag(ITEM_TAGS, 'minecraft:fences', 'ic2:iron_fence')
for code in out_lang: out_lang[code]['block.ic2.iron_fence'] = name(code, 'blockFenceIron', 'Iron Fence')
LEGACY['blockFenceIron'] = 'ic2:iron_fence'
# scaffolds, rubber / resin sheets, reinforced glass and door
def simple_state(bid, model): write(os.path.join(A, 'blockstates', bid + '.json'), {'variants': {'': {'model': model}}})
def self_loot(bid, item=None, extra=None):
    pool = {'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': item or 'ic2:' + bid}], 'conditions': [{'condition': 'minecraft:survives_explosion'}]}
    if extra: pool['conditions'] = extra
    write(os.path.join(D, 'ic2/loot_table/blocks', bid + '.json'), {'type': 'minecraft:block', 'pools': [pool]})
for bid, tex, lang, reinforced in (('scaffold', 'blockScaffold', 'blockScaffold', 5), ('iron_scaffold', 'blockIronScaffold', 'blockIronScaffold', 12)):
    copy(os.path.join(OTEX, 'blocks', tex + '.png'), os.path.join(A, 'textures/block', bid + '.png'))
    copy(os.path.join(OTEX, 'blocks', tex + '.reinforced.png'), os.path.join(A, 'textures/block', bid + '_reinforced.png'))
    for suffix in ('', '_reinforced'):
        write(os.path.join(A, 'models/block', bid + suffix + '.json'), {'parent': 'minecraft:block/cube_all', 'render_type': 'minecraft:cutout', 'textures': {'all': 'ic2:block/' + bid + suffix}})
    write(os.path.join(A, 'blockstates', bid + '.json'), {'variants': {f'strength={s}': {'model': 'ic2:block/' + bid + ('_reinforced' if s == reinforced else '')} for s in range(13)}})
    write(os.path.join(A, 'models/item', bid + '.json'), {'parent': 'ic2:block/' + bid})
    self_loot(bid)
    add_tag(BLOCK_TAGS, 'minecraft:mineable/' + ('pickaxe' if bid == 'iron_scaffold' else 'axe'), 'ic2:' + bid)
    for code in out_lang: out_lang[code]['block.ic2.' + bid] = name(code, lang, bid)
    LEGACY[lang] = 'ic2:' + bid
copy(os.path.join(OTEX, 'blocks/blockRubber.png'), os.path.join(A, 'textures/block/rubber_sheet.png'))
copy(os.path.join(OTEX, 'blocks/blockHarz.png'), os.path.join(A, 'textures/block/resin_sheet.png'))
for bid in ('rubber_sheet', 'resin_sheet'):
    write(os.path.join(A, 'models/block', bid + '.json'), {'parent': 'minecraft:block/block', 'render_type': 'minecraft:cutout', 'textures': {'particle': 'ic2:block/' + bid, 't': 'ic2:block/' + bid},
        'elements': [{'from': [0, 0, 0], 'to': [16, 2, 16], 'faces': {f: {'texture': '#t', 'uv': [0, 14, 16, 16] if f not in ('up', 'down') else [0, 0, 16, 16], **({'cullface': f} if f != 'up' else {})} for f in FACES}}]})
    write(os.path.join(A, 'blockstates', bid + '.json'), {'variants': {'': {'model': 'ic2:block/' + bid}} if bid == 'resin_sheet' else {'supported=false': {'model': 'ic2:block/' + bid}, 'supported=true': {'model': 'ic2:block/' + bid}}})
write(os.path.join(A, 'models/item/rubber_sheet.json'), {'parent': 'ic2:block/rubber_sheet'})
self_loot('rubber_sheet')
write(os.path.join(D, 'ic2/loot_table/blocks/resin_sheet.json'), {'type': 'minecraft:block', 'pools': [{'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': 'ic2:resin'}], 'conditions': [{'condition': 'minecraft:survives_explosion'}, {'condition': 'minecraft:random_chance', 'chance': 0.8}]}]})
for code in out_lang:
    out_lang[code]['block.ic2.rubber_sheet'] = name(code, 'blockRubber', 'Rubber Sheet')
    out_lang[code]['block.ic2.resin_sheet'] = name(code, 'blockHarz', 'Resin')
    out_lang[code]['block.ic2.reinforced_glass'] = name(code, 'blockAlloyGlass', 'Reinforced Glass')
    out_lang[code]['block.ic2.reinforced_door'] = out_lang[code]['item.ic2.reinforced_door'] = name(code, 'itemDoorAlloy', 'Reinforced Door')
LEGACY.update({'blockRubber': 'ic2:rubber_sheet', 'blockAlloyGlass': 'ic2:reinforced_glass', 'itemDoorAlloy': 'ic2:reinforced_door'})
copy(os.path.join(OTEX, 'blocks/blockAlloyGlass.png'), os.path.join(A, 'textures/block/reinforced_glass.png'))
write(os.path.join(A, 'models/block/reinforced_glass.json'), {'parent': 'minecraft:block/cube_all', 'render_type': 'minecraft:cutout', 'textures': {'all': 'ic2:block/reinforced_glass'}})
simple_state('reinforced_glass', 'ic2:block/reinforced_glass')
write(os.path.join(A, 'models/item/reinforced_glass.json'), {'parent': 'ic2:block/reinforced_glass'})
self_loot('reinforced_glass', extra=[{'condition': 'minecraft:match_tool', 'predicate': {'predicates': {'minecraft:enchantments': [{'enchantments': 'minecraft:silk_touch', 'levels': {'min': 1}}]}}}])
add_tag(BLOCK_TAGS, 'minecraft:mineable/pickaxe', 'ic2:reinforced_glass')
add_tag(BLOCK_TAGS, 'minecraft:impermeable', 'ic2:reinforced_glass')
add_tag(BLOCK_TAGS, 'c:glass_blocks', 'ic2:reinforced_glass')
# reinforced door from the vanilla door templates
copy(os.path.join(OTEX, 'blocks/blockDoorAlloy.top.png'), os.path.join(A, 'textures/block/reinforced_door_top.png'))
copy(os.path.join(OTEX, 'blocks/blockDoorAlloy.bottom.png'), os.path.join(A, 'textures/block/reinforced_door_bottom.png'))
copy(os.path.join(OTEX, 'items/itemDoorAlloy.png'), os.path.join(A, 'textures/item/reinforced_door.png'))
write(os.path.join(A, 'models/item/reinforced_door.json'), {'parent': 'minecraft:item/generated', 'textures': {'layer0': 'ic2:item/reinforced_door'}})
door_variants = {}
for part in ('bottom_left', 'bottom_left_open', 'bottom_right', 'bottom_right_open', 'top_left', 'top_left_open', 'top_right', 'top_right_open'):
    write(os.path.join(A, 'models/block', 'reinforced_door_' + part + '.json'), {'parent': 'minecraft:block/door_' + part, 'render_type': 'minecraft:cutout', 'textures': {'bottom': 'ic2:block/reinforced_door_bottom', 'top': 'ic2:block/reinforced_door_top'}})
ROT = {'east': 0, 'south': 90, 'west': 180, 'north': 270}
for facing, rot in ROT.items():
    for half in ('lower', 'upper'):
        for hinge in ('left', 'right'):
            for open_ in ('false', 'true'):
                part = ('bottom' if half == 'lower' else 'top') + '_' + hinge
                y = rot
                if open_ == 'true':
                    part += '_open'
                    y = (rot + (90 if hinge == 'left' else 270)) % 360
                v = {'model': 'ic2:block/reinforced_door_' + part}
                if y: v['y'] = y
                door_variants[f'facing={facing},half={half},hinge={hinge},open={open_}'] = v
write(os.path.join(A, 'blockstates/reinforced_door.json'), {'variants': door_variants})
write(os.path.join(D, 'ic2/loot_table/blocks/reinforced_door.json'), {'type': 'minecraft:block', 'pools': [{'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': 'ic2:reinforced_door',
    'conditions': [{'condition': 'minecraft:block_state_property', 'block': 'ic2:reinforced_door', 'properties': {'half': 'lower'}}]}], 'conditions': [{'condition': 'minecraft:survives_explosion'}]}]})
for t in ('minecraft:mineable/pickaxe', 'minecraft:doors'): add_tag(BLOCK_TAGS, t, 'ic2:reinforced_door')
add_tag(ITEM_TAGS, 'minecraft:doors', 'ic2:reinforced_door')
# construction foam, reinforced foam and the sixteen foam wall colours
for bid, tex, lang, drop in (('foam', 'blockFoam', 'blockFoam', None), ('reinforced_foam', 'blockReinforcedFoam', 'blockReinforcedFoam', 'ic2:iron_scaffold')):
    copy(os.path.join(OTEX, 'blocks/cf', tex + '.png'), os.path.join(A, 'textures/block', bid + '.png'))
    write(os.path.join(A, 'models/block', bid + '.json'), {'parent': 'minecraft:block/cube_all', 'textures': {'all': 'ic2:block/' + bid}})
    write(os.path.join(A, 'blockstates', bid + '.json'), {'variants': {'': {'model': 'ic2:block/' + bid}}})
    write(os.path.join(A, 'models/item', bid + '.json'), {'parent': 'ic2:block/' + bid})
    if drop: write(os.path.join(D, 'ic2/loot_table/blocks', bid + '.json'), {'type': 'minecraft:block', 'pools': [{'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': drop}]}]})
    for code in out_lang: out_lang[code]['block.ic2.' + bid] = name(code, lang, bid)
    LEGACY[lang] = 'ic2:' + bid
wall_variants = {}
for i, c in enumerate(COLORS):
    copy(os.path.join(OTEX, 'blocks/cf', f'blockWall.{c}.png'), os.path.join(A, 'textures/block/foam_wall', f'{i}.png'))
    write(os.path.join(A, 'models/block/foam_wall', f'{i}.json'), {'parent': 'minecraft:block/cube_all', 'textures': {'all': f'ic2:block/foam_wall/{i}'}})
    wall_variants[f'color={i}'] = {'model': f'ic2:block/foam_wall/{i}'}
write(os.path.join(A, 'blockstates/foam_wall.json'), {'variants': wall_variants})
for code in out_lang: out_lang[code]['block.ic2.foam_wall'] = name(code, 'blockFoam', 'Construction Foam')
add_tag(BLOCK_TAGS, 'minecraft:mineable/pickaxe', 'ic2:foam_wall')
LEGACY['blockWall'] = 'ic2:foam_wall'
# mining pipe shaft (4/16 wide) and drill tip; both drop the pipe item, which cannot be placed by hand
copy(os.path.join(OTEX, 'blocks/machine/blockMiningPipe.png'), os.path.join(A, 'textures/block/mining_pipe.png'))
copy(os.path.join(OTEX, 'blocks/machine/blockMiningTip.png'), os.path.join(A, 'textures/block/mining_pipe_tip.png'))
write(os.path.join(A, 'models/block/mining_pipe.json'), {'parent': 'minecraft:block/block', 'textures': {'particle': 'ic2:block/mining_pipe', 'side': 'ic2:block/mining_pipe'}, 'elements': [{'from': [6, 0, 6], 'to': [10, 16, 10],
    'faces': {f: {'uv': [6, 0, 10, 16] if f not in ('up', 'down') else [6, 6, 10, 10], 'texture': '#side'} for f in FACES}}]})
write(os.path.join(A, 'models/block/mining_pipe_tip.json'), {'parent': 'minecraft:block/cube_all', 'textures': {'all': 'ic2:block/mining_pipe_tip'}})
for bid in ('mining_pipe', 'mining_pipe_tip'):
    write(os.path.join(A, 'blockstates', bid + '.json'), {'variants': {'': {'model': 'ic2:block/' + bid}}})
    write(os.path.join(D, 'ic2/loot_table/blocks', bid + '.json'), {'type': 'minecraft:block', 'pools': [{'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': 'ic2:mining_pipe'}], 'conditions': [{'condition': 'minecraft:survives_explosion'}]}]})
    add_tag(BLOCK_TAGS, 'minecraft:mineable/pickaxe', 'ic2:' + bid)
write(os.path.join(A, 'models/item/mining_pipe.json'), {'parent': 'minecraft:item/generated', 'textures': {'layer0': 'ic2:block/mining_pipe'}})
for code in out_lang:
    out_lang[code]['block.ic2.mining_pipe'] = out_lang[code]['item.ic2.mining_pipe'] = name(code, 'blockMiningPipe', 'Mining Pipe')
    out_lang[code]['block.ic2.mining_pipe_tip'] = name(code, 'blockMiningTip', 'Mining Pipe')
LEGACY['blockMiningPipe'] = 'ic2:mining_pipe'
for tag, e in [('minecraft:logs', 'ic2:rubber_wood'), ('minecraft:logs_that_burn', 'ic2:rubber_wood'), ('minecraft:mineable/axe', 'ic2:rubber_wood'), ('minecraft:leaves', 'ic2:rubber_leaves'),
               ('minecraft:mineable/hoe', 'ic2:rubber_leaves'), ('minecraft:saplings', 'ic2:rubber_sapling')]:
    add_tag(BLOCK_TAGS, tag, e)
for tag, e in [('minecraft:logs', 'ic2:rubber_wood'), ('minecraft:logs_that_burn', 'ic2:rubber_wood'), ('minecraft:leaves', 'ic2:rubber_leaves'), ('minecraft:saplings', 'ic2:rubber_sapling'), ('c:rubber_woods', 'ic2:rubber_wood')]:
    add_tag(ITEM_TAGS, tag, e)
LEGACY['OreDict:woodRubber'] = 'ic2:rubber_wood'

# ---------- deepslate ore variants: the IC2 ore pixels (difference to the 1.7.10 stone) laid over vanilla deepslate
VAN = os.path.join(ROOT, 'related/vanilla')
def vanilla(version, member, local):
    path = os.path.join(VAN, local)
    if not os.path.exists(path):  # taken from the local Prism Launcher library jars
        import zipfile
        jar = os.path.expanduser(f'~/.local/share/PrismLauncher/libraries/com/mojang/minecraft/{version}/minecraft-{version}-client.jar')
        os.makedirs(os.path.dirname(path), exist_ok=True)
        with zipfile.ZipFile(jar) as z, open(path, 'wb') as f: f.write(z.read(member))
    return Image.open(path).convert('RGBA')
stone_old = vanilla('1.7.10', 'assets/minecraft/textures/blocks/stone.png', 'v1710/stone.png')
deepslate = vanilla('1.21.1', 'assets/minecraft/textures/block/deepslate.png', 'v121/deepslate.png')
DEEP = [('tin', 'blockOreTin', 'Tin', 'minecraft:needs_stone_tool', 'tin_ingot'), ('uranium', 'blockOreUran', 'Uranium', 'minecraft:needs_iron_tool', None),
        ('lead', 'blockOreLead', 'Lead', 'minecraft:needs_stone_tool', 'lead_ingot')]
for mat, leg, en, needs, ingot in DEEP:
    bid = f'deepslate_{mat}_ore'
    ore = Image.open(os.path.join(OTEX, 'blocks', leg + '.png')).convert('RGBA')
    out = deepslate.copy()
    for px in range(16):
        for py in range(16):
            if ore.getpixel((px, py)) != stone_old.getpixel((px, py)): out.putpixel((px, py), ore.getpixel((px, py)))
    path = os.path.join(A, 'textures/block', bid + '.png'); os.makedirs(os.path.dirname(path), exist_ok=True); out.save(path)
    write(os.path.join(A, 'models/block', bid + '.json'), {'parent': 'minecraft:block/cube_all', 'textures': {'all': 'ic2:block/' + bid}})
    write(os.path.join(A, 'blockstates', bid + '.json'), {'variants': {'': {'model': 'ic2:block/' + bid}}})
    write(os.path.join(A, 'models/item', bid + '.json'), {'parent': 'ic2:block/' + bid})
    write(os.path.join(D, 'ic2/loot_table/blocks', bid + '.json'), {'type': 'minecraft:block', 'pools': [{'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': 'ic2:' + bid}], 'conditions': [{'condition': 'minecraft:survives_explosion'}]}]})
    for t in (f'c:ores/{mat}', 'c:ores', 'c:ores_in_ground/deepslate', 'c:ore_rates/singular', 'minecraft:mineable/pickaxe', needs): add_tag(BLOCK_TAGS, t, 'ic2:' + bid)
    for t in (f'c:ores/{mat}', 'c:ores', 'c:ores_in_ground/deepslate'): add_tag(ITEM_TAGS, t, 'ic2:' + bid)
    for code in out_lang:
        base_name = name(code, leg, en + ' Ore')
        out_lang[code]['block.ic2.' + bid] = ('Глубинносланцевая ' + base_name[0].lower() + base_name[1:]) if code == 'ru_ru' else 'Deepslate ' + base_name
    if ingot:  # the original smelts tin and lead ore (0.5 xp); blasting like vanilla ores
        for variant in (mat + '_ore', bid):
            write(os.path.join(D, 'ic2/recipe', f'{ingot}_from_smelting_{variant}.json'), {'type': 'minecraft:smelting', 'category': 'misc', 'group': ingot, 'ingredient': {'item': 'ic2:' + variant}, 'result': {'id': 'ic2:' + ingot}, 'experience': 0.5, 'cookingtime': 200})
            write(os.path.join(D, 'ic2/recipe', f'{ingot}_from_blasting_{variant}.json'), {'type': 'minecraft:blasting', 'category': 'misc', 'group': ingot, 'ingredient': {'item': 'ic2:' + variant}, 'result': {'id': 'ic2:' + ingot}, 'experience': 0.5, 'cookingtime': 100})

# ---------- furnace recipes of IC2.java (dusts, crushed ores, resin, rubber wood, raw crystal memory)
ING = {'iron': 'minecraft:iron_ingot', 'gold': 'minecraft:gold_ingot', 'copper': 'minecraft:copper_ingot', 'tin': 'ic2:tin_ingot', 'lead': 'ic2:lead_ingot', 'silver': 'ic2:silver_ingot'}
SMELT = [('ic2:rubber_wood', 'minecraft:jungle_log', 0.1), ('ic2:bronze_dust', 'ic2:bronze_ingot', 0), ('ic2:hydrated_coal_dust', 'ic2:coal_dust', 0), ('ic2:resin', 'ic2:rubber', 0.3),
         ('ic2:raw_crystal_memory', 'ic2:crystal_memory', 0)]
for m, ing in ING.items():
    SMELT += [(f'ic2:{m}_dust', ing, 0), (f'ic2:crushed_{m}_ore', ing, 0), (f'ic2:purified_crushed_{m}_ore', ing, 0)]
for src, dst, xp in SMELT:
    write(os.path.join(D, 'ic2/recipe', 'smelting_' + src.split(':')[1] + '.json'), {'type': 'minecraft:smelting', 'category': 'misc', 'ingredient': {'item': src}, 'result': {'id': dst}, 'experience': xp, 'cookingtime': 200})

# ---------- world generation
# The original spread ores over 0..sea level and scaled vein counts with that height. 1.21 ground below sea level is -64..63, twice
# as tall: heights map linearly (old y -> 2 * old - 64) and counts double, so density per block stays the same. Stone is replaced by
# the normal ore, deepslate by the deepslate variant, like vanilla ores.
WG = os.path.join(D, 'ic2/worldgen')
ORES = [('tin_ore', 'tin', 6, 50, -64, 14), ('uranium_ore', 'uranium', 3, 40, -64, 62), ('lead_ore', 'lead', 4, 16, -64, 62)]
for fid, mat, size, count, lo, hi in ORES:
    write(os.path.join(WG, 'configured_feature', fid + '.json'), {'type': 'minecraft:ore', 'config': {'size': size, 'discard_chance_on_air_exposure': 0,
        'targets': [{'target': {'predicate_type': 'minecraft:tag_match', 'tag': 'minecraft:stone_ore_replaceables'}, 'state': {'Name': f'ic2:{mat}_ore'}},
                    {'target': {'predicate_type': 'minecraft:tag_match', 'tag': 'minecraft:deepslate_ore_replaceables'}, 'state': {'Name': f'ic2:deepslate_{mat}_ore'}}]}})
    write(os.path.join(WG, 'placed_feature', fid + '.json'), {'feature': 'ic2:' + fid, 'placement': [{'type': 'minecraft:count', 'count': count}, {'type': 'minecraft:in_square'},
        {'type': 'minecraft:height_range', 'height': {'type': 'minecraft:uniform', 'min_inclusive': {'absolute': lo}, 'max_inclusive': {'absolute': hi}}}, {'type': 'minecraft:biome'}]})
write(os.path.join(WG, 'configured_feature/rubber_tree.json'), {'type': 'ic2:rubber_tree', 'config': {}})
write(os.path.join(WG, 'placed_feature/rubber_tree.json'), {'feature': 'ic2:rubber_tree', 'placement': [{'type': 'minecraft:biome'}]})
write(os.path.join(D, 'ic2/neoforge/biome_modifier/ores.json'), {'type': 'neoforge:add_features', 'biomes': '#minecraft:is_overworld', 'features': ['ic2:' + o[0] for o in ORES], 'step': 'underground_ores'})
write(os.path.join(D, 'ic2/neoforge/biome_modifier/rubber_tree.json'), {'type': 'neoforge:add_features', 'biomes': '#ic2:rubber_tree_biomes', 'features': 'ic2:rubber_tree', 'step': 'vegetal_decoration'})
write(os.path.join(D, 'ic2/tags/worldgen/biome/rubber_tree_biomes.json'), {'values': [{'id': '#c:is_swamp', 'required': False}, '#minecraft:is_forest', '#minecraft:is_jungle']})

# ---------- gui, sounds
for f in os.listdir(os.path.join(OTEX, 'gui')):
    copy(os.path.join(OTEX, 'gui', f), os.path.join(A, 'textures/gui', f.lower()))
SOUNDS = {'machines.macerator_op': 'Machines/MaceratorOp', 'machines.extractor_op': 'Machines/ExtractorOp', 'machines.compressor_op': 'Machines/CompressorOp',
          'machines.recycler_op': 'Machines/RecyclerOp', 'machines.interrupt_one': 'Machines/InterruptOne', 'machines.electro_furnace_loop': 'Machines/Electro Furnace/ElectroFurnaceLoop',
          'machines.iron_furnace_op': 'Machines/IronFurnaceOp', 'generators.generator_loop': 'Generators/GeneratorLoop', 'generators.geothermal_loop': 'Generators/GeothermalLoop',
          'generators.watermill_loop': 'Generators/WatermillLoop', 'generators.windgen_loop': 'Generators/WindGenLoop', 'tools.wrench': 'Tools/wrench', 'generators.mass_fab_loop': 'Generators/MassFabricator/MassFabLoop', 'generators.mass_fab_scrap': 'Generators/MassFabricator/MassFabScrapSolo', 'tools.treetap': 'Tools/Treetap',
          'machines.miner_op': 'Machines/MinerOp', 'machines.pump_op': 'Machines/PumpOp', 'tools.od_scanner': 'Tools/ODScanner', 'tools.painter': 'Tools/Painter', 'machines.terraformer_loop': 'Terraformers/TerraformerGenericloop', 'tools.dynamite_remote': 'Tools/dynamiteomote', 'generators.nuclear_reactor.loop': 'Generators/NuclearReactor/NuclearReactorLoop', 'generators.nuclear_reactor.geiger_low': 'Generators/NuclearReactor/GeigerLowEU', 'generators.nuclear_reactor.geiger_med': 'Generators/NuclearReactor/GeigerMedEU', 'generators.nuclear_reactor.geiger_high': 'Generators/NuclearReactor/GeigerHighEU',
          'machines.o_mat': 'Machines/o-mat', 'machines.teleporter.charged_loop': 'Machines/Teleporter/TeleChargedLoop', 'machines.teleporter.use': 'Machines/Teleporter/TeleUse',
          'tools.mining_laser.mining': 'Tools/MiningLaser/MiningLaser', 'tools.mining_laser.low_focus': 'Tools/MiningLaser/MiningLaserLowFocus', 'tools.mining_laser.long_range': 'Tools/MiningLaser/MiningLaserLongRange',
          'tools.mining_laser.scatter': 'Tools/MiningLaser/MiningLaserScatter', 'tools.mining_laser.explosive': 'Tools/MiningLaser/MiningLaserExplosive'}
sounds_json = {}
for key, src in SOUNDS.items():
    s = os.path.join(JAR, 'ic2/sounds', src + '.ogg')
    if not os.path.exists(s): warnings.append('missing sound ' + src); continue
    dst = key.replace('.', '/')
    copy(s, os.path.join(A, 'sounds', dst + '.ogg'))
    sounds_json[key] = {'sounds': [{'name': 'ic2:' + dst}]}
write(os.path.join(A, 'sounds.json'), sounds_json)

# ---------- crops
exec(compile(open(os.path.join(ROOT, "tools/crops.py")).read(), "tools/crops.py", "exec"))

gen_item('iron_turning_blank', 'itemTurningBlanksIRON.png', 'itemTurningBlanksIRON', ['itemTurningBlanks', 'itemTurningBlanks@0'])
gen_item('wood_turning_blank', 'itemTurningBlanksWOOD.png', 'itemTurningBlanksWOOD', ['itemTurningBlanksWood', 'itemTurningBlanksWood@0'])
gen_item('lathing_tool', 'itemLathingTool.png', 'itemLathingTool', ['itemLathingTool'])
for blank in ('iron_turning_blank', 'wood_turning_blank'):
    write(os.path.join(A, 'models/item', blank + '.json'), {'parent': 'minecraft:builtin/entity', 'gui_light': 'front', 'textures': {'particle': 'ic2:item/' + blank}})
for material in ('IRON', 'WOOD'):
    copy(os.path.join(OTEX, 'items/turnables', material + '.png'), os.path.join(A, 'textures/item/turnables', material.lower() + '.png'))
copy(os.path.join(OTEX, 'gui/GUINuke.png'), os.path.join(A, 'textures/gui/guinuke.png'))
gen_item('steam_turbine', 'itemSteamTurbine.png', 'itemSteamTurbine', ['itemSteamTurbine'])

exec(compile(open(os.path.join(ROOT, "tools/luminator.py")).read(), "tools/luminator.py", "exec"))

# ---------- recipes
VANILLA = {'reeds': 'sugar_cane', 'snow_layer': 'snow', 'snow': 'snow_block', 'fence': 'oak_fence', 'trapdoor': 'oak_trapdoor', 'wooden_door': 'oak_door',
           'wooden_pressure_plate': 'oak_pressure_plate', 'tallgrass': 'short_grass', 'red_flower': 'poppy', 'yellow_flower': 'dandelion', 'waterlily': 'lily_pad',
           'brick_block': 'bricks', 'netherbrick': 'nether_brick', 'nether_brick': 'nether_bricks', 'hardened_clay': 'terracotta', 'mossy_cobblestone': 'mossy_cobblestone',
           'stone_slab': 'smooth_stone_slab', 'double_plant': 'sunflower', 'record_blocks': 'music_disc_blocks', 'record_cat': 'music_disc_cat', 'record_chirp': 'music_disc_chirp',
           'record_far': 'music_disc_far', 'record_stal': 'music_disc_stal', 'record_strad': 'music_disc_strad', 'record_wait': 'music_disc_wait', 'record_ward': 'music_disc_ward',
           'golden_apple': 'golden_apple'}
VANILLA_ANY_TAG = {'log': '#minecraft:logs', 'planks': '#minecraft:planks', 'wool': '#minecraft:wool', 'sapling': '#minecraft:saplings', 'stained_hardened_clay': '#minecraft:terracotta'}
VANILLA_META = {('dye', '4'): 'lapis_lazuli', ('dye', '14'): 'orange_dye', ('dye', '15'): 'bone_meal', ('dye', '3'): 'cocoa_beans', ('dye', '2'): 'green_dye', ('dye', '1'): 'red_dye',
                ('dye', '0'): 'ink_sac', ('coal', '1'): 'charcoal', ('golden_apple', '1'): 'enchanted_golden_apple', ('sandstone', '0'): 'sandstone', ('stone', '0'): 'stone',
                ('quartz_block', '0'): 'quartz_block', ('sapling', '0'): 'oak_sapling', ('log', '0'): 'oak_log', ('planks', '0'): 'oak_planks', ('wool', '0'): 'white_wool',
                ('log', '1'): 'spruce_log', ('log', '2'): 'birch_log', ('log', '3'): 'jungle_log', ('log2', '0'): 'acacia_log', ('log2', '1'): 'dark_oak_log',
                ('planks', '1'): 'spruce_planks', ('planks', '2'): 'birch_planks', ('planks', '3'): 'jungle_planks', ('planks', '4'): 'acacia_planks', ('planks', '5'): 'dark_oak_planks'}

def resolve(token, for_output=False):
    """'IC2:itemDust@3' / 'minecraft:x@meta' / 'OreDict:name' -> ('item', id) | ('tag', id) | None. Count handled by caller."""
    token = token.strip()
    if token.startswith('Fluid:'):
        return ('tag', 'ic2:fluid_containers/' + token[6:].split('@')[0])
    if token.startswith('OreDict:'):
        n = token[8:].split('@')[0]
        t = content.oredict_tag(n)
        if t is None: warnings.append('oredict ' + n); return None
        return ('tag', t)
    mod, rest = token.split(':', 1)
    parts = rest.split('@')
    n, meta = parts[0], (parts[1] if len(parts) > 1 else None)
    if mod == 'IC2':
        for key in ([f'{n}@{meta}'] if meta not in (None, '*', '-1', '32767') else []) + ([f'{n}@0'] if meta in (None, '0') else []) + [n]:
            if key in LEGACY: return ('item', LEGACY[key])
        return None
    if mod == 'minecraft':
        if meta not in (None, '*', '-1', '32767') and (n, meta) in VANILLA_META: return ('item', 'minecraft:' + VANILLA_META[(n, meta)])
        if meta not in (None, '*', '-1', '32767', '0') and n in ('dye', 'wool', 'log', 'planks', 'sapling', 'stone', 'coal', 'sandstone', 'stained_hardened_clay', 'red_flower', 'double_plant', 'tallgrass', 'golden_apple'):
            warnings.append('vanilla meta ' + token); return None
        if not for_output and meta in ('*', '-1', '32767') and n in VANILLA_ANY_TAG: return ('tag', VANILLA_ANY_TAG[n][1:])
        if n in VANILLA_META and n not in VANILLA: pass
        mapped = VANILLA.get(n, n)
        if (n, '0') in VANILLA_META and n not in VANILLA: mapped = VANILLA_META[(n, '0')]
        return ('item', 'minecraft:' + mapped)
    return None

def parse_stack(token, for_output=False):
    parts = token.strip().split('@')
    count = int(parts[2]) if len(parts) > 2 else 1
    r = resolve('@'.join(parts[:2]), for_output)
    return r, count

def ini_lines(name):
    buf = ''
    for line in open(os.path.join(JAR, 'assets/ic2/config', name), encoding='utf8'):
        line = line.split(';')[0].rstrip()
        if not line.strip(): continue
        if line.endswith('\\'): buf += line[:-1].strip() + ' '; continue
        yield (buf + line.strip()).strip(); buf = ''

def machine(name, out):
    recipes = []
    for line in ini_lines(name):
        if '=' not in line: continue
        left, right = [s.strip() for s in line.split('=', 1)]
        inp, cnt = parse_stack(left)
        if inp is None: warnings.append(f'{name}: input {left}'); continue
        outs, meta, ok = [], {}, True
        for tok in right.split():
            if tok.startswith('@'):
                if ':' in tok: k, v = tok[1:].split(':'); meta[k] = int(v)
                continue
            o, c = parse_stack(tok, True)
            if o is None or o[0] != 'item': ok = False; warnings.append(f'{name}: output {tok}'); break
            outs.append({'item': o[1], 'count': c})
        if not ok: continue
        r = {'input': {inp[0]: inp[1], 'count': cnt}, 'output': outs}
        if meta: r['meta'] = meta
        recipes.append(r)
    write(os.path.join(RES, 'ic2/recipes', out + '.json'), recipes)
    return len(recipes)

for ini, out in [('macerator.ini', 'macerator'), ('extractor.ini', 'extractor'), ('compressor.ini', 'compressor'), ('ore_washer.ini', 'ore_washer'),
                 ('thermal_centrifuge.ini', 'thermal_centrifuge'), ('blast_furnace.ini', 'blast_furnace'), ('block_cutter.ini', 'block_cutter'),
                 ('metal_former_cutting.ini', 'metal_former_cutting'), ('metal_former_extruding.ini', 'metal_former_extruding'), ('metal_former_rolling.ini', 'metal_former_rolling')]:
    print(out, machine(ini, out))
blacklist = []
for tok in 'minecraft:glass_pane, minecraft:stick, minecraft:snowball, minecraft:snow_layer, minecraft:snow, IC2:blockScaffold'.split(','):
    r = resolve(tok)
    if r: blacklist.append({r[0]: r[1]})
write(os.path.join(RES, 'ic2/recipes/recycler_blacklist.json'), blacklist)

def ingredient(alts):
    ret = []
    for a in alts.split('|'):
        shaped_blank = re.fullmatch(r'IC2:(itemTurningBlanks|itemTurningBlanksWood)@(\d+)', a.strip())
        if shaped_blank and int(shaped_blank[2]) != 0:
            packed = int(shaped_blank[2])
            state = [(packed >> (4 * i)) & 15 for i in range(4, -1, -1)]
            iid = 'wood_turning_blank' if shaped_blank[1].endswith('Wood') else 'iron_turning_blank'
            ret.append({'type': 'neoforge:components', 'items': 'ic2:' + iid, 'components': {'ic2:lathe_state': state}, 'strict': False})
            continue
        r, _ = parse_stack(a)
        if r is None: return None
        ret.append({r[0]: r[1]})
    return ret[0] if len(ret) == 1 else ret

crafting = 0
WRITTEN_RECIPES = set()
def crafting_recipes(name, shaped):
    global crafting
    for line in ini_lines(name):
        if '=' not in line: continue
        left, right = [s.strip() for s in line.split('=', 1)]
        res, _ = parse_stack(left, True)
        if res is None or res[0] != 'item': warnings.append(f'{name}: result {left}'); continue
        for n, alt in enumerate(re.split(r',\s*(?=")', right) if shaped else [a for a in re.split(r'\s*,\s*', right) if a.strip()]):
            count = 1
            m = re.search(r'@count:(\d+)', alt)
            if m: count = int(m.group(1)); alt = alt.replace(m.group(0), '')
            alt = re.sub(r'@hidden|@consuming\S*|@ignoreSameInputOutput', '', alt)
            rid = res[1].split(':')[1] + ('' if n == 0 else f'_{n}') + ('' if shaped else '_shapeless')
            if shaped:
                pm = re.match(r'"([^"]*)"\s*(.*)', alt.strip())
                if not pm: warnings.append(f'{name}: bad {alt}'); continue
                pattern = pm.group(1).split('|')
                pattern = [p for p in pattern if p != ''] if any(pattern) else pattern
                width = max(len(p) for p in pattern)
                pattern = [p.ljust(width) for p in pattern]
                keys, ok = {}, True
                for kv in pm.group(2).split():
                    k, v = kv.split(':', 1)
                    ing = ingredient(v)
                    if ing is None: ok = False; warnings.append(f'{name}: {left} key {kv}'); break
                    keys[k] = ing
                if not ok: continue
                used = set(''.join(pattern)) - {' '}
                if not used <= set(keys): warnings.append(f'{name}: {left} missing keys'); continue
                obj = {'type': 'ic2:shaped', 'category': 'misc', 'pattern': pattern, 'key': {k: v for k, v in keys.items() if k in used}, 'result': {'id': res[1], 'count': count}}
            else:
                ings, ok = [], True
                for tok in alt.split():
                    ing = ingredient(tok)
                    if ing is None: ok = False; warnings.append(f'{name}: {left} ing {tok}'); break
                    ings.append(ing)
                if not ok: continue
                if len(ings) == 1 and ings[0] == {'item': res[1]}: continue  # 1.7.10 damage migration recipes (x@1 = x)
                obj = {'type': 'ic2:shapeless', 'category': 'misc', 'ingredients': ings, 'result': {'id': res[1], 'count': count}}
            base_rid, k = rid, 1
            while rid in WRITTEN_RECIPES: rid = f'{base_rid}_alt{k}'; k += 1  # several ini lines with the same result
            WRITTEN_RECIPES.add(rid)
            write(os.path.join(D, 'ic2/recipe', rid + '.json'), obj)
            crafting += 1

crafting_recipes('shaped_recipes.ini', True)
crafting_recipes('shapeless_recipes.ini', False)
print('crafting', crafting)

# ---------- tags & lang
for tag, entries in ITEM_TAGS.items():
    ns, path = tag.split(':')
    write(os.path.join(D, ns, 'tags/item', path + '.json'), {'values': entries})
for tag, entries in BLOCK_TAGS.items():
    ns, path = tag.split(':')
    write(os.path.join(D, ns, 'tags/block', path + '.json'), {'values': entries})
write(os.path.join(D, 'ic2/tags/item/fluid_containers/water.json'), {'values': ['minecraft:water_bucket', 'ic2:water_cell']})
write(os.path.join(D, 'ic2/tags/item/fluid_containers/lava.json'), {'values': ['minecraft:lava_bucket', 'ic2:lava_cell']})
write(os.path.join(D, 'ic2/tags/item/tools/forge_hammer.json'), {'values': ['ic2:forge_hammer']})
write(os.path.join(D, 'ic2/tags/item/tools/wire_cutter.json'), {'values': ['ic2:cutter']})
for code in out_lang:
    out_lang[code]['effect.ic2.radiation'] = name(code, 'potion.radiation', 'Radiation')
    out_lang[code]['entity.ic2.mining_laser'] = name(code, 'itemToolMiningLaser', 'Mining Laser')
for f in ('laser.png', 'beam.png', 'newsafe.png'): copy(os.path.join(OTEX, 'models', f), os.path.join(A, 'textures/models', f))
# UU world scan values (1.7.10 names flattened to 1.21 ids)
COLORS16 = ['white', 'orange', 'magenta', 'light_blue', 'yellow', 'lime', 'pink', 'gray', 'light_gray', 'cyan', 'purple', 'blue', 'brown', 'green', 'red', 'black']
FLAT = {'log': ['oak_log', 'spruce_log', 'birch_log', 'jungle_log'], 'log2': ['acacia_log', 'dark_oak_log'], 'sapling': ['oak_sapling', 'spruce_sapling', 'birch_sapling', 'jungle_sapling', 'acacia_sapling', 'dark_oak_sapling'],
        'planks': ['oak_planks', 'spruce_planks', 'birch_planks', 'jungle_planks', 'acacia_planks', 'dark_oak_planks'], 'sand': ['sand', 'red_sand'], 'sandstone': ['sandstone', 'chiseled_sandstone', 'cut_sandstone'],
        'stained_hardened_clay': [c + '_terracotta' for c in COLORS16], 'wool': [c + '_wool' for c in COLORS16], 'dye': [None, None, None, 'cocoa_beans', 'lapis_lazuli'],
        'red_flower': ['poppy', 'blue_orchid', 'allium', 'azure_bluet', 'red_tulip', 'orange_tulip', 'white_tulip', 'pink_tulip', 'oxeye_daisy'], 'double_plant': ['sunflower', 'lilac', None, None, 'rose_bush', 'peony'],
        'stone_slab': ['smooth_stone_slab', 'sandstone_slab']}
RENAME = {'hardened_clay': 'terracotta', 'yellow_flower': 'dandelion', 'waterlily': 'lily_pad', 'reeds': 'sugar_cane', 'melon': 'melon_slice', 'stone_stairs': 'cobblestone_stairs',
          'wooden_door': 'oak_door', 'wooden_pressure_plate': 'oak_pressure_plate', 'fence': 'oak_fence', 'record_11': 'music_disc_11'}
uu = {}
for line in open(os.path.join(JAR, 'assets/ic2/config/uu_scan_values.ini'), encoding='utf8'):
    line = line.split(';')[0].strip()
    if '=' not in line: continue
    k, v = [x.strip() for x in line.split('=', 1)]
    mod, rest = k.split(':', 1)
    name_, meta = (rest.split('@') + ['0'])[:2]
    meta = int(meta)
    target = None
    if mod == 'minecraft':
        if name_ in FLAT: target = FLAT[name_][meta] if meta < len(FLAT[name_]) else None
        elif name_.startswith('record_'): target = 'music_disc_' + name_[7:]
        elif meta == 0: target = RENAME.get(name_, name_)
        if target: target = 'minecraft:' + target
    elif mod == 'IC2':
        target = {'itemIngot': 'minecraft:copper_ingot', 'itemIngot@1': 'ic2:tin_ingot', 'blockOreCopper': 'minecraft:copper_ore'}.get(rest) or LEGACY.get(rest)
    if target: uu[target] = min(float(v), uu.get(target, float('inf')))
    else: warnings.append('uu value ' + k)
uu['ic2:iridium_ore'] = min(uu.get('ic2:iridium_ore', 1e18), 12000.0)
for t in list(uu):
    if t in ('ic2:tin_ore', 'ic2:lead_ore', 'ic2:uranium_ore'): uu['ic2:deepslate_' + t[4:]] = uu[t]
    if t == 'minecraft:copper_ore': uu['minecraft:deepslate_copper_ore'] = uu[t]
write(os.path.join(RES, 'ic2/uu_scan_values.json'), uu)
# damage types of the original IC2DamageSource (death messages keep their vanilla keys)
for dt in ('electricity', 'radiation', 'nuke'):
    write(os.path.join(D, 'ic2/damage_type', dt + '.json'), {'message_id': dt, 'exhaustion': 0.1, 'scaling': 'when_caused_by_living_non_player'})
write(os.path.join(D, 'minecraft/tags/damage_type/bypasses_armor.json'), {'values': ['ic2:radiation']})
for code, entries in out_lang.items(): write(os.path.join(A, 'lang', code + '.json'), dict(sorted(entries.items())))
with open(os.path.join(ROOT, 'build/gen-warnings.txt') if os.path.isdir(os.path.join(ROOT, 'build')) else '/dev/null', 'w') as f: f.write('\n'.join(warnings))
print('warnings', len(warnings))
