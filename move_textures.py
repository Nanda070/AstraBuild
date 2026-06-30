import os
import shutil

source_dir = r'C:\Users\adnan\Documents\coding\AstraBuild_Pack\astrabuild-bb-art\textures'
dest_dir = r'C:\Users\adnan\Documents\coding\AstraBuild_Pack\assets\minecraft\textures\astrabuild'

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

for filename in os.listdir(source_dir):
    if filename.endswith(".png"):
        file_id = filename.split("_")[0]
        if file_id in id_to_name:
            new_name = id_to_name[file_id] + ".png"
            src_path = os.path.join(source_dir, filename)
            dest_path = os.path.join(dest_dir, new_name)
            shutil.copy2(src_path, dest_path)
            print(f"Copied {filename} -> {new_name}")
        else:
            print(f"Skipped {filename} (Unknown ID)")
