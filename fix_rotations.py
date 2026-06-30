import os
import json

models_dir = r"C:\Users\adnan\Documents\coding\Astra_Pack\assets\minecraft\models\astrabuild"

for filename in os.listdir(models_dir):
    if not filename.endswith('.json'):
        continue
    
    filepath = os.path.join(models_dir, filename)
    with open(filepath, 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    # Remove rotation if angle is 0
    if 'elements' in data:
        for el in data['elements']:
            if 'rotation' in el:
                if el['rotation'].get('angle') == 0:
                    del el['rotation']
                elif str(el['rotation'].get('angle')) not in ['45', '22.5', '-22.5', '-45', '45.0', '22.5', '-22.5', '-45.0']:
                    # Just remove invalid rotations entirely, to be safe. Blockbench often outputs weird angles.
                    del el['rotation']

    with open(filepath, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=4)

print("Fixed rotations!")
