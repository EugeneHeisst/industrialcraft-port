# Executed by gen.py with original-resource helpers and tag maps in scope.
LEGACY['blockLuminatorDark'] = LEGACY['blockLuminator'] = 'ic2:luminator'
for code in out_lang:
    out_lang[code]['block.ic2.luminator'] = name(code, 'blockLuminatorDark', 'Luminator')
    out_lang[code]['ic2.jetpack.hover.enabled'] = 'Режим зависания включён.' if code == 'ru_ru' else 'Hover Mode enabled.'
    out_lang[code]['ic2.jetpack.hover.disabled'] = 'Режим зависания выключен.' if code == 'ru_ru' else 'Hover Mode disabled.'
for active in (False, True):
    copy(os.path.join(OTEX, 'blocks/wiring', 'blockLuminator' + ('' if active else 'Dark') + '.png'), os.path.join(A, 'textures/block', 'luminator_' + str(active).lower() + '.png'))
full_boxes = {'up': ([0,0,0],[16,1,16]), 'down': ([0,15,0],[16,16,16]), 'north': ([0,0,15],[16,16,16]), 'south': ([0,0,0],[16,16,1]), 'west': ([15,0,0],[16,16,16]), 'east': ([0,0,0],[1,16,16])}
narrow_boxes = {'up': full_boxes['up'], 'down': ([4,13,4],[12,16,12]), 'north': ([6,3,14],[10,13,16]), 'south': ([6,3,0],[10,13,2]), 'west': ([14,3,6],[16,13,10]), 'east': ([0,3,6],[2,13,10])}
variants = {}
for direction in FACES:
    for active in (False, True):
        for full in (False, True):
            model = f'luminator_{direction}_{str(active).lower()}_{str(full).lower()}'
            low, high = (full_boxes if full else narrow_boxes)[direction]
            write(os.path.join(A, 'models/block', model + '.json'), {'parent': 'minecraft:block/block', 'textures': {'all': 'ic2:block/luminator_' + str(active).lower(), 'particle': 'ic2:block/luminator_false'}, 'elements': [{'from': low, 'to': high, 'faces': {f: {'texture': '#all'} for f in FACES}}]})
            variants[f'facing={direction},active={str(active).lower()},full_cover={str(full).lower()}'] = {'model': 'ic2:block/' + model}
write(os.path.join(A, 'blockstates/luminator.json'), {'variants': variants})
write(os.path.join(A, 'models/item/luminator.json'), {'parent': 'ic2:block/luminator_up_false_false'})
write(os.path.join(D, 'ic2/loot_table/blocks/luminator.json'), {'type': 'minecraft:block', 'pools': []})
gen_item('fuel_jetpack', 'itemArmorJetpack.png', 'itemArmorJetpack', ['itemArmorJetpack', 'itemArmorJetpack@18001'])
