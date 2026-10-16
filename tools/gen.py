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
        out_lang[code][k if k.startswith('itemGroup') else 'ic2.' + k] = v.replace('%d', '%s')

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
gen_item('forge_hammer', 'itemToolForgeHammer.png', 'itemToolForgeHammer', ['itemToolForgeHammer'])
gen_item('cutter', 'itemToolCutter.png', 'itemToolCutter', ['itemToolCutter'])
gen_item('single_use_battery', 'itemBatSU.png', 'itemBatSU', ['itemBatSU'])
for iid, leg in [('re_battery', 'itemBatRE'), ('advanced_re_battery', 'itemAdvBat'), ('energy_crystal', 'itemBatCrystal'), ('lapotron_crystal', 'itemBatLamaCrystal')]:
    LEGACY[leg] = 'ic2:' + iid
    overrides = []
    for i in range(5):
        copy(os.path.join(OTEX, 'items', f'{leg}.{i}.png'), os.path.join(A, 'textures/item', f'{iid}_{i}.png'))
        write(os.path.join(A, 'models/item', f'{iid}_{i}.json'), {'parent': 'minecraft:item/generated', 'textures': {'layer0': f'ic2:item/{iid}_{i}'}})
        if i: overrides.append({'predicate': {'ic2:charge_level': i / 4 - 0.01}, 'model': f'ic2:item/{iid}_{i}'})
    write(os.path.join(A, 'models/item', iid + '.json'), {'parent': 'minecraft:item/generated', 'textures': {'layer0': f'ic2:item/{iid}_0'}, 'overrides': overrides})
    for code in out_lang: out_lang[code]['item.ic2.' + iid] = name(code, leg, iid)
LEGACY['itemBatREDischarged'] = 'ic2:re_battery'
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
        write(os.path.join(A, 'models/block', bid + '.json'), {'parent': 'minecraft:block/cube_all', 'textures': {'all': names[0]}})
        write(os.path.join(A, 'blockstates', bid + '.json'), {'variants': {'': {'model': 'ic2:block/' + bid}}})
        write(os.path.join(A, 'models/item', bid + '.json'), {'parent': 'ic2:block/' + bid})
    else:
        variants = {}
        for facing in range(6):
            for active in (0, 1):
                tex = {FACES[side]: names[SIDE_MAP[facing][side] + 6 * active] for side in range(6)}
                tex['particle'] = tex['north']
                mname = f'{bid}_{FACES[facing]}' + ('_active' if active else '')
                write(os.path.join(A, 'models/block', mname + '.json'), {'parent': 'minecraft:block/cube', 'textures': tex})
                variants[f'active={"true" if active else "false"},facing={FACES[facing]}'] = {'model': 'ic2:block/' + mname}
        write(os.path.join(A, 'blockstates', bid + '.json'), {'variants': variants})
        write(os.path.join(A, 'models/item', bid + '.json'), {'parent': f'ic2:block/{bid}_south'})
    drop = b.get('drop', 'self')
    drop_item = 'ic2:' + bid if drop == 'self' else 'ic2:' + drop
    write(os.path.join(D, 'ic2/loot_table/blocks', bid + '.json'), {'type': 'minecraft:block', 'pools': [{'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': drop_item}], 'conditions': [{'condition': 'minecraft:survives_explosion'}]}]})

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
    variants = [('', 0)] + ([('.' + c, i + 1) for i, c in enumerate(COLORS)] if colorable else [])
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
for tag, e in [('minecraft:logs', 'ic2:rubber_wood'), ('minecraft:logs_that_burn', 'ic2:rubber_wood'), ('minecraft:mineable/axe', 'ic2:rubber_wood'), ('minecraft:leaves', 'ic2:rubber_leaves'),
               ('minecraft:mineable/hoe', 'ic2:rubber_leaves'), ('minecraft:saplings', 'ic2:rubber_sapling')]:
    add_tag(BLOCK_TAGS, tag, e)
for tag, e in [('minecraft:logs', 'ic2:rubber_wood'), ('minecraft:logs_that_burn', 'ic2:rubber_wood'), ('minecraft:leaves', 'ic2:rubber_leaves'), ('minecraft:saplings', 'ic2:rubber_sapling'), ('c:rubber_woods', 'ic2:rubber_wood')]:
    add_tag(ITEM_TAGS, tag, e)
LEGACY['OreDict:woodRubber'] = 'ic2:rubber_wood'

# ---------- world generation (original sea level 63 -> base height 64)
WG = os.path.join(D, 'ic2/worldgen')
ORES = [('tin_ore', 'ic2:tin_ore', 6, 25, 0, 39), ('uranium_ore', 'ic2:uranium_ore', 3, 20, 0, 63), ('lead_ore', 'ic2:lead_ore', 4, 8, 0, 63)]
for fid, block_id, size, count, lo, hi in ORES:
    write(os.path.join(WG, 'configured_feature', fid + '.json'), {'type': 'minecraft:ore', 'config': {'size': size, 'discard_chance_on_air_exposure': 0,
        'targets': [{'target': {'predicate_type': 'minecraft:tag_match', 'tag': 'minecraft:stone_ore_replaceables'}, 'state': {'Name': block_id}}]}})
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
          'generators.watermill_loop': 'Generators/WatermillLoop', 'generators.windgen_loop': 'Generators/WindGenLoop', 'tools.wrench': 'Tools/wrench', 'tools.treetap': 'Tools/Treetap'}
sounds_json = {}
for key, src in SOUNDS.items():
    s = os.path.join(JAR, 'ic2/sounds', src + '.ogg')
    if not os.path.exists(s): warnings.append('missing sound ' + src); continue
    dst = key.replace('.', '/')
    copy(s, os.path.join(A, 'sounds', dst + '.ogg'))
    sounds_json[key] = {'sounds': [{'name': 'ic2:' + dst}]}
write(os.path.join(A, 'sounds.json'), sounds_json)

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
                ('quartz_block', '0'): 'quartz_block', ('sapling', '0'): 'oak_sapling', ('log', '0'): 'oak_log', ('planks', '0'): 'oak_planks', ('wool', '0'): 'white_wool'}

def resolve(token, for_output=False):
    """'IC2:itemDust@3' / 'minecraft:x@meta' / 'OreDict:name' -> ('item', id) | ('tag', id) | None. Count handled by caller."""
    token = token.strip()
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
        r, _ = parse_stack(a)
        if r is None: return None
        ret.append({r[0]: r[1]})
    return ret[0] if len(ret) == 1 else ret

crafting = 0
def crafting_recipes(name, shaped):
    global crafting
    for line in ini_lines(name):
        if '=' not in line: continue
        left, right = [s.strip() for s in line.split('=', 1)]
        res, _ = parse_stack(left, True)
        if res is None or res[0] != 'item': warnings.append(f'{name}: result {left}'); continue
        for n, alt in enumerate(re.split(r',\s*(?=")', right) if shaped else [right]):
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
                obj = {'type': 'minecraft:crafting_shaped', 'category': 'misc', 'pattern': pattern, 'key': {k: v for k, v in keys.items() if k in used}, 'result': {'id': res[1], 'count': count}}
            else:
                ings, ok = [], True
                for tok in alt.split():
                    ing = ingredient(tok)
                    if ing is None: ok = False; warnings.append(f'{name}: {left} ing {tok}'); break
                    ings.append(ing)
                if not ok: continue
                obj = {'type': 'minecraft:crafting_shapeless', 'category': 'misc', 'ingredients': ings, 'result': {'id': res[1], 'count': count}}
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
write(os.path.join(D, 'ic2/tags/item/tools/forge_hammer.json'), {'values': ['ic2:forge_hammer']})
write(os.path.join(D, 'ic2/tags/item/tools/wire_cutter.json'), {'values': ['ic2:cutter']})
for code, entries in out_lang.items(): write(os.path.join(A, 'lang', code + '.json'), dict(sorted(entries.items())))
with open(os.path.join(ROOT, 'build/gen-warnings.txt') if os.path.isdir(os.path.join(ROOT, 'build')) else '/dev/null', 'w') as f: f.write('\n'.join(warnings))
print('warnings', len(warnings))
