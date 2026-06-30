import os
import re

build_gradle = r'build.gradle.kts'
with open(build_gradle, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('tasks {', 'tasks.withType<JavaCompile> {\n    options.encoding = "UTF-8"\n}\n\ntasks {')
with open(build_gradle, 'w', encoding='utf-8') as f:
    f.write(content)

print("Updated build.gradle.kts")

files_to_fix = [
    r'src\main\java\com\astrabuild\gui\ReactorGui.java',
    r'src\main\java\com\astrabuild\gui\EnergyStorageGui.java',
    r'src\main\java\com\astrabuild\gui\CentrifugeGui.java',
    r'src\main\java\com\astrabuild\gui\MassFabricatorGui.java',
    r'src\main\java\com\astrabuild\machine\NuclearReactorCore.java'
]

replacements = {
    'ReactorGui.java': [
        (r'deserialize\(".*?"\)\);', r'deserialize("§cЯдерный Реактор"));'),
        (r'deserialize\(".*?"\)', r'deserialize("§cЯдерный Реактор")')
    ],
    'EnergyStorageGui.java': [
        (r'Bukkit\.createInventory\(this, 27, LegacyComponentSerializer\.legacySection\(\)\.deserialize\(".*?"\)\);', r'Bukkit.createInventory(this, 27, LegacyComponentSerializer.legacySection().deserialize("§aМФСУ"));')
    ],
    'CentrifugeGui.java': [
        (r'Bukkit\.createInventory\(this, 27, LegacyComponentSerializer\.legacySection\(\)\.deserialize\(".*?"\)\);', r'Bukkit.createInventory(this, 27, LegacyComponentSerializer.legacySection().deserialize("§eЦентрифуга"));')
    ],
    'MassFabricatorGui.java': [
        (r'Bukkit\.createInventory\(this, 27, LegacyComponentSerializer\.legacySection\(\)\.deserialize\(".*?"\)\);', r'Bukkit.createInventory(this, 27, LegacyComponentSerializer.legacySection().deserialize("§dГенератор Материи"));')
    ],
    'NuclearReactorCore.java': [
        (r'hm\.updateHologram\(coreLocation, ".*?\n.*?\n.*?"\);', r'hm.updateHologram(coreLocation, "§c[РЕАКТОР СЛОМАН]\n§7Нужна база 3x3\n§7и 4 железных столба");'),
        (r'String text = ".*?\n" \+ heatColor \+ ".*?: " \+ heat \+ " / 10000\\n" \+ ".*? " \+ energy \+ " / " \+ max;', r'String text = "§b⚡ Ядерный Реактор\n" + heatColor + "🔥 Температура: " + heat + " / 10000\n" + "§e⚡ Энергия: " + energy + " / " + max;')
    ]
}

for file_path in files_to_fix:
    if os.path.exists(file_path):
        with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
            content = f.read()
        
        filename = os.path.basename(file_path)
        if filename in replacements:
            for pattern, repl in replacements[filename]:
                content = re.sub(pattern, repl, content, flags=re.DOTALL)
        
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Fixed {filename}")