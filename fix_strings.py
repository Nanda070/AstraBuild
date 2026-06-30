import os
import re

files_to_fix = [
    r'C:\Users\adnan\Documents\coding\AstraBuild\src\main\java\com\astrabuild\gui\CentrifugeGui.java',
    r'C:\Users\adnan\Documents\coding\AstraBuild\src\main\java\com\astrabuild\gui\ConnectorInsertGui.java',
    r'C:\Users\adnan\Documents\coding\AstraBuild\src\main\java\com\astrabuild\gui\EnergyStorageGui.java',
    r'C:\Users\adnan\Documents\coding\AstraBuild\src\main\java\com\astrabuild\gui\ReactorGui.java',
    r'C:\Users\adnan\Documents\coding\AstraBuild\src\main\java\com\astrabuild\machine\NuclearReactorCore.java'
]

replacements = {
    'CentrifugeGui.java': [
        (r'deserialize\(".*?"\)', r'deserialize("§eЦентрифуга")'),
        (r'meta.displayName\(.*?\)', r'meta.displayName(LegacyComponentSerializer.legacySection().deserialize("§eСтатус: Центрифуга"))'),
        (r'meta\.lore\(List\.of\(.*?\}\)\)\);', r'''meta.lore(List.of(
                    LegacyComponentSerializer.legacySection().deserialize("§7Запас энергии: §a" + energy + " §7/ §a" + storage.getMaxEnergy("CENTRIFUGE") + " EU"),
                    LegacyComponentSerializer.legacySection().deserialize("§7Потребление: §c500 EU/операция"),
                    LegacyComponentSerializer.legacySection().deserialize(""),
                    LegacyComponentSerializer.legacySection().deserialize("§fВход: Руда (Необр. Уран)"),
                    LegacyComponentSerializer.legacySection().deserialize("§fВыход: Обогащ. Уран и Изотопы")
            ));''')
    ],
    'ConnectorInsertGui.java': [
        (r'deserialize\(".*?"\)', r'deserialize("§8Настройки Коннектора")')
    ],
    'EnergyStorageGui.java': [
        (r'Bukkit\.createInventory\(.*?\)', r'Bukkit.createInventory(this, 27, LegacyComponentSerializer.legacySection().deserialize("§aМФСУ"))'),
        (r'meta\.displayName\(.*? deserialize\(".*?"\)\)', r'meta.displayName(LegacyComponentSerializer.legacySection().deserialize("§aСтатус МФСУ"))'),
        (r'greenMeta\.displayName\(.*?\)', r'greenMeta.displayName(LegacyComponentSerializer.legacySection().deserialize("§aЗаряжается"))'),
        (r'redMeta\.displayName\(.*?\)', r'redMeta.displayName(LegacyComponentSerializer.legacySection().deserialize("§cРазряжается"))'),
        (r'meta\.lore\(List\.of\(.*?\}\)\)\);', r'''meta.lore(List.of(
                    LegacyComponentSerializer.legacySection().deserialize("§7Запас энергии: §a" + energy + " §7/ §a" + max + " EU"),
                    LegacyComponentSerializer.legacySection().deserialize(""),
                    LegacyComponentSerializer.legacySection().deserialize("§fВход: Труба (в любой блок)"),
                    LegacyComponentSerializer.legacySection().deserialize("§fВыход: Труба (из любого блока)")
            ));''')
    ],
    'ReactorGui.java': [
        (r'Bukkit\.createInventory\(.*?\)', r'Bukkit.createInventory(this, 27, LegacyComponentSerializer.legacySection().deserialize("§cЯдерный Реактор"))'),
        (r'meta\.displayName\(.*? deserialize\(".*?"\)\)', r'meta.displayName(LegacyComponentSerializer.legacySection().deserialize("§cСтатус Реактора"))'),
        (r'meta\.lore\(List\.of\(.*?\}\)\)\);', r'''meta.lore(List.of(
                    LegacyComponentSerializer.legacySection().deserialize("§7Температура: " + heatColor + heat + " §7/ §c" + maxHeat),
                    LegacyComponentSerializer.legacySection().deserialize("§7Хладагент: §b" + coolant + " mB"),
                    LegacyComponentSerializer.legacySection().deserialize(""),
                    LegacyComponentSerializer.legacySection().deserialize("§fОхлаждение: §aВода, Синий лед (вокруг ядра)"),
                    LegacyComponentSerializer.legacySection().deserialize("§fВыход энергии: §e1000 EU/тик")
            ));''')
    ],
    'NuclearReactorCore.java': [
        (r'hm\.updateHologram\(coreLocation, ".*?\n.*?\n.*?"\)', r'hm.updateHologram(coreLocation, "§c[РЕАКТОР СЛОМАН]\\n§7Нужна база 3x3\\n§7и 4 железных столба")'),
        (r'String text = ".*?\\n" \+\s*heatColor \+ ".*?: " \+ heat \+ " \/ 10000\\n" \+\s*".*?: " \+ energy \+ " \/ " \+ max;', 
         r'String text = "§b⚡ Ядерный Реактор\\n" + heatColor + "🔥 Температура: " + heat + " / 10000\\n" + "§e⚡ Энергия: " + energy + " / " + max;')
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