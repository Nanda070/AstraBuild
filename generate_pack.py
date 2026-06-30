import os
import json
import shutil

pack_path = r'C:\Users\adnan\Documents\coding\AstraBuild_Pack'

os.makedirs(os.path.join(pack_path, 'assets', 'minecraft', 'models', 'item'), exist_ok=True)
os.makedirs(os.path.join(pack_path, 'assets', 'minecraft', 'models', 'astrabuild'), exist_ok=True)
os.makedirs(os.path.join(pack_path, 'assets', 'minecraft', 'textures', 'astrabuild'), exist_ok=True)

mcmeta = {
    'pack': {
        'pack_format': 15,
        'description': 'AstraBuild Premium Resource Pack'
    }
}
with open(os.path.join(pack_path, 'pack.mcmeta'), 'w') as f:
    json.dump(mcmeta, f, indent=2)

items = [
  {'id': 458001, 'name': 'quarry', 'parent': 'blast_furnace', 'vanilla': 'block/blast_furnace'},
  {'id': 458002, 'name': 'pipe', 'parent': 'cyan_stained_glass_pane', 'vanilla': 'item/generated'},
  {'id': 458003, 'name': 'connector_insert', 'parent': 'dropper', 'vanilla': 'block/dropper'},
  {'id': 458004, 'name': 'upgrade_fortune', 'parent': 'lapis_lazuli', 'vanilla': 'item/generated'},
  {'id': 458005, 'name': 'upgrade_silk_touch', 'parent': 'gold_ingot', 'vanilla': 'item/generated'},
  {'id': 458006, 'name': 'upgrade_overclocker', 'parent': 'redstone', 'vanilla': 'item/generated'},
  {'id': 458007, 'name': 'satellite_terminal', 'parent': 'barrel', 'vanilla': 'block/barrel'},
  {'id': 458008, 'name': 'orbital_remote', 'parent': 'golden_hoe', 'vanilla': 'item/handheld'},
  {'id': 458009, 'name': 'coolant_tank', 'parent': 'cauldron', 'vanilla': 'item/cauldron'},
  {'id': 458010, 'name': 'reactor_core', 'parent': 'furnace', 'vanilla': 'block/furnace'},
  {'id': 458011, 'name': 'centrifuge', 'parent': 'dispenser', 'vanilla': 'block/dispenser'},
  {'id': 458012, 'name': 'mass_fabricator', 'parent': 'dropper', 'vanilla': 'block/dropper'},
  {'id': 458013, 'name': 'energy_storage', 'parent': 'redstone_block', 'vanilla': 'block/redstone_block'},
  {'id': 458014, 'name': 'nano_chestplate', 'parent': 'netherite_chestplate', 'vanilla': 'item/generated'},
  {'id': 458015, 'name': 'nano_sword', 'parent': 'netherite_sword', 'vanilla': 'item/handheld'},
  {'id': 458016, 'name': 'uranium_raw', 'parent': 'raw_iron', 'vanilla': 'item/generated'},
  {'id': 458017, 'name': 'uranium_isotope', 'parent': 'emerald', 'vanilla': 'item/generated'},
  {'id': 458018, 'name': 'fuel_rod', 'parent': 'end_rod', 'vanilla': 'block/end_rod'},
  {'id': 458019, 'name': 'depleted_fuel_rod', 'parent': 'end_rod', 'vanilla': 'block/end_rod'},
  {'id': 458020, 'name': 'toxic_waste', 'parent': 'slime_ball', 'vanilla': 'item/generated'},
  {'id': 458021, 'name': 'hazmat_helmet', 'parent': 'leather_helmet', 'vanilla': 'item/generated'},
  {'id': 458022, 'name': 'hazmat_chestplate', 'parent': 'leather_chestplate', 'vanilla': 'item/generated'},
  {'id': 458023, 'name': 'hazmat_leggings', 'parent': 'leather_leggings', 'vanilla': 'item/generated'},
  {'id': 458024, 'name': 'hazmat_boots', 'parent': 'leather_boots', 'vanilla': 'item/generated'}
]

# Create individual astrabuild models
for item in items:
    is_handheld = 'item/handheld' if item['vanilla'] == 'item/handheld' else 'item/generated'
    custom_json = {
        'parent': f'minecraft:{is_handheld}',
        'textures': {
            'layer0': f'minecraft:astrabuild/{item["name"]}'
        }
    }
    with open(os.path.join(pack_path, 'assets', 'minecraft', 'models', 'astrabuild', f'{item["name"]}.json'), 'w') as f:
        json.dump(custom_json, f, indent=2)

# Group items by parent for overrides
parents = {}
for item in items:
    if item['parent'] not in parents:
        parents[item['parent']] = []
    parents[item['parent']].append(item)

for parent_name, grouped_items in parents.items():
    parent_type = grouped_items[0]['vanilla']
    override_json = {
        'parent': f'minecraft:{parent_type}'
    }
    
    if parent_type.startswith('item/'):
        override_json['textures'] = { 'layer0': f'minecraft:item/{parent_name}' }
        
    overrides = []
    for item in sorted(grouped_items, key=lambda x: x['id']):
        overrides.append({
            'predicate': { 'custom_model_data': item['id'] },
            'model': f'minecraft:astrabuild/{item["name"]}'
        })
    override_json['overrides'] = overrides
    
    with open(os.path.join(pack_path, 'assets', 'minecraft', 'models', 'item', f'{parent_name}.json'), 'w') as f:
        json.dump(override_json, f, indent=2)

print("Resource pack successfully generated at:", pack_path)
