#!/usr/bin/env python3
"""Verify the distributable contains source resources and every IC2 model/texture/sound dependency."""
import hashlib
import json
from pathlib import Path
import sys
import zipfile

root = Path(__file__).resolve().parents[1]
jar = Path(sys.argv[1])
errors = []
with zipfile.ZipFile(jar) as archive:
    names = set(archive.namelist())
    assert len(names) == len(archive.namelist()), 'duplicate JAR entries'
    source_count = 0
    for source in ('src/main/resources', 'src/generated/resources'):
        for path in (root / source).rglob('*'):
            if not path.is_file():
                continue
            relative = path.relative_to(root / source).as_posix()
            if relative not in names:
                errors.append('missing source resource ' + relative)
            elif relative != 'META-INF/neoforge.mods.toml' and path.read_bytes() != archive.read(relative):
                errors.append('stale source resource ' + relative)
            source_count += 1
    def require(resource, prefix, extension, origin):
        if not isinstance(resource, str) or not resource.startswith('ic2:'):
            return
        path = 'assets/ic2/' + prefix + '/' + resource.split(':', 1)[1] + extension
        if path not in names:
            errors.append(origin + ' -> missing ' + path)
    for name in names:
        if name.startswith('assets/ic2/models/') and name.endswith('.json'):
            model = json.loads(archive.read(name))
            require(model.get('parent'), 'models', '.json', name)
            for texture in model.get('textures', {}).values():
                require(texture, 'textures', '.png', name)
            for override in model.get('overrides', []):
                require(override['model'], 'models', '.json', name)
        elif name.startswith('assets/ic2/blockstates/') and name.endswith('.json'):
            def visit(value):
                if isinstance(value, dict):
                    if 'model' in value:
                        require(value['model'], 'models', '.json', name)
                    for child in value.values(): visit(child)
                elif isinstance(value, list):
                    for child in value: visit(child)
            visit(json.loads(archive.read(name)))
    for event in json.loads(archive.read('assets/ic2/sounds.json')).values():
        for sound in event.get('sounds', []):
            if isinstance(sound, dict):
                if sound.get('type') == 'event': continue
                sound = sound['name']
            require(sound, 'sounds', '.ogg', 'sounds.json')
    required = ['assets/ic2/textures/mob_effect/radiation.png', 'data/c/tags/item/ingots/uranium.json', 'ic2/loot_injections.json']
    for name in required:
        if name not in names: errors.append('required issue fix missing: ' + name)
    ore_manifest = json.loads((root / 'tools/ore-textures/manifest.json').read_text())
    for filename, expected in ore_manifest.items():
        name = 'assets/ic2/textures/item/' + filename
        if name not in names or hashlib.sha256(archive.read(name)).hexdigest() != expected:
            errors.append('user ore texture missing/changed: ' + name)
    counts = {kind: sum(n.startswith(prefix) for n in names if not n.endswith('/')) for kind, prefix in {
        'textures': 'assets/ic2/textures/', 'models': 'assets/ic2/models/', 'translations': 'assets/ic2/lang/',
        'recipes': 'data/ic2/recipe/', 'tags': 'data/c/tags/', 'loot_tables': 'data/ic2/loot_table/'}.items()}
    print(json.dumps({'jar': str(jar), 'bytes': jar.stat().st_size, 'sha256': hashlib.sha256(jar.read_bytes()).hexdigest(),
        'source_resources_checked': source_count, 'user_ore_textures_verified': len(ore_manifest), 'counts': counts, 'errors': sorted(errors)}, indent=2))
if errors: sys.exit(1)
