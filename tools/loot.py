"""IC2 2.2.828 IC2Loot entries: same stack ranges/weights, mapped onto modern vanilla chest pools."""
def injections():
    def item(name, low, high, weight):
        e = {'type': 'minecraft:item', 'name': name if ':' in name else 'ic2:' + name, 'weight': weight}
        if low != 1 or high != 1:
            e['functions'] = [{'function': 'minecraft:set_count', 'count': low if low == high else {'type': 'minecraft:uniform', 'min': low, 'max': high}}]
        return e
    tools = [item('bronze_' + n, 1, 1, 3) for n in ('pickaxe', 'sword', 'helmet', 'chestplate', 'leggings', 'boots')]
    ingots = [item('minecraft:copper_ingot', 2, 6, 9), item('tin_ingot', 1, 5, 8)]
    data = {
        'abandoned_mineshaft': [item('bronze_pickaxe', 1, 1, 1), item('filled_tin_can', 4, 16, 8), item('iridium_shard', 2, 5, 8)] + ingots,
        'desert_pyramid': tools + ingots,
        'jungle_temple': tools + ingots,
        'stronghold_corridor': [item('iridium_ore', 1, 4, 1), item('iridium_shard', 4, 14, 8)] + tools + ingots,
        'stronghold_crossing': [item('bronze_pickaxe', 1, 1, 1), item('iridium_ore', 1, 1, 1), item('iridium_shard', 3, 7, 4)] + tools + ingots,
        'village/village_weaponsmith': [item('bronze_ingot', 2, 4, 5), item('iridium_shard', 3, 7, 4)] + tools + ingots + [item('rubber_sapling', 1, 4, 4)],
        'spawn_bonus_chest': [item('treetap', 1, 1, 2)],
        'simple_dungeon': [item('minecraft:copper_ingot', 2, 5, 100), item('tin_ingot', 2, 5, 100), item('iridium_ore', 1, 2, 20), item('iridium_shard', 6, 14, 100)] + [item('minecraft:music_disc_' + n, 1, 1, 5) for n in ('blocks', 'chirp', 'far', 'mall', 'mellohi', 'stal', 'strad', 'ward', '11', 'wait')],
    }
    return {'minecraft:chests/' + name: entries for name, entries in data.items()}
