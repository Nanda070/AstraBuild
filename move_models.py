import os
import shutil

source_dir = r'C:\Users\adnan\Documents\coding\AstraBuild_Pack\new'
dest_dir = r'C:\Users\adnan\Documents\coding\AstraBuild_Pack\assets\minecraft\models\astrabuild'

id_to_name = {
    "458001": "quarry",
    "458002": "pipe",
    "458003": "connector_insert",
    "458004": "upgrade_fortune",
    "458005": "upgrade_silk_touch",
    "458006": "upgrade_overclocker",
    "458007": "satellite_terminal",
    "458008": "orbital_remote",
    "458009": "coolant_tank",
    "458010": "reactor_core",
    "458011": "centrifuge",
    "458012": "mass_fabricator",
    "458013": "energy_storage",
    "458014": "nano_chestplate",
    "458015": "nano_sword",
    "458016": "uranium_raw",
    "458017": "uranium_isotope",
    "458018": "fuel_rod",
    "458019": "depleted_fuel_rod",
    "458020": "toxic_waste",
    "458021": "hazmat_helmet",
    "458022": "hazmat_chestplate",
    "458023": "hazmat_leggings",
    "458024": "hazmat_boots"
}

os.makedirs(dest_dir, exist_ok=True)

if os.path.exists(source_dir):
    for filename in os.listdir(source_dir):
        if filename.endswith(".json"):
            file_id = filename.split("_")[0]
            if file_id in id_to_name:
                new_name = id_to_name[file_id] + ".json"
                src_path = os.path.join(source_dir, filename)
                dest_path = os.path.join(dest_dir, new_name)
                # Read JSON and fix texture paths
                with open(src_path, 'r', encoding='utf-8') as f:
                    content = f.read()
                
                # Replace texture paths if they point to the old blockbench names
                for key, val in id_to_name.items():
                    old_tex_name = filename.replace(".json", "")
                    content = content.replace(f'"{old_tex_name}"', f'"astrabuild/{val}"')
                    content = content.replace(f'"texture": "{old_tex_name}"', f'"texture": "astrabuild/{val}"')
                    # Just in case Blockbench exported it as a flat texture name
                
                with open(dest_path, 'w', encoding='utf-8') as f:
                    f.write(content)
                
                print(f"Moved and updated {filename} -> {new_name}")

# Now rename the entire pack folder
old_pack = r'C:\Users\adnan\Documents\coding\AstraBuild_Pack'
new_pack = r'C:\Users\adnan\Documents\coding\Astra_Pack'

try:
    if os.path.exists(old_pack) and not os.path.exists(new_pack):
        os.rename(old_pack, new_pack)
        print("Successfully renamed AstraBuild_Pack to Astra_Pack!")
except Exception as e:
    print(f"Error renaming folder: {e}")
