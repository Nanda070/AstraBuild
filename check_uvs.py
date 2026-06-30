import os
import json

models_dir = r"C:\Users\adnan\Documents\coding\Astra_Pack\assets\minecraft\models\astrabuild"

invalid_uvs = []

for filename in os.listdir(models_dir):
    if not filename.endswith('.json'):
        continue
    
    filepath = os.path.join(models_dir, filename)
    with open(filepath, 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    if 'elements' in data:
        for el in data['elements']:
            if 'faces' in el:
                for face, face_data in el['faces'].items():
                    if 'uv' in face_data:
                        uv = face_data['uv']
                        if any(v < -16 or v > 32 for v in uv):  # Minecraft allows slightly out of bounds, but let's see
                            invalid_uvs.append((filename, uv))

print("Invalid UVs:", invalid_uvs)
