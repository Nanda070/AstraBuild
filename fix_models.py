import os
import json

models_dir = r"C:\Users\adnan\Documents\coding\Astra_Pack\assets\minecraft\models\astrabuild"

for filename in os.listdir(models_dir):
    if not filename.endswith('.json'):
        continue
    
    filepath = os.path.join(models_dir, filename)
    with open(filepath, 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    # Remove Bedrock/Blockbench specific fields
    data.pop('format_version', None)
    data.pop('groups', None)
    
    # Fix texture mapping
    tex_name = filename.replace('.json', '')
    if 'textures' in data:
        for k, v in data['textures'].items():
            if v == "item/placeholder":
                data['textures'][k] = f"astrabuild/{tex_name}"
    
    # Add GUI display scaling so it fits in inventory
    if 'display' not in data:
        data['display'] = {
            "gui": {
                "rotation": [30, 225, 0],
                "translation": [0, 0, 0],
                "scale": [0.625, 0.625, 0.625]
            },
            "ground": {
                "rotation": [0, 0, 0],
                "translation": [0, 3, 0],
                "scale": [0.25, 0.25, 0.25]
            },
            "fixed": {
                "rotation": [0, 0, 0],
                "translation": [0, 0, 0],
                "scale": [0.5, 0.5, 0.5]
            },
            "thirdperson_righthand": {
                "rotation": [75, 45, 0],
                "translation": [0, 2.5, 0],
                "scale": [0.375, 0.375, 0.375]
            },
            "firstperson_righthand": {
                "rotation": [0, 45, 0],
                "translation": [0, 0, 0],
                "scale": [0.40, 0.40, 0.40]
            },
            "firstperson_lefthand": {
                "rotation": [0, 225, 0],
                "translation": [0, 0, 0],
                "scale": [0.40, 0.40, 0.40]
            }
        }
        
    with open(filepath, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=4)

print("Fixed all models!")
