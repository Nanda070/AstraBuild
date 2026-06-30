import os
import re

files_to_fix = [
    r'AstraBuild\src\main\java\com\astrabuild\item\RecipeRegistry.java',
    r'AstraBuild\src\main\java\com\astrabuild\item\UraniumItems.java',
    r'AstraBuild\src\main\java\com\astrabuild\machine\EnergyStorageWorker.java'
]

replacements = {
    'RecipeRegistry.java': [
        (r'createSimpleMachineItem\("CENTRIFUGE", Material.DISPENSER, ".*?", ".*?"\)', r'createSimpleMachineItem("CENTRIFUGE", Material.DISPENSER, "§eЦентрифуга", "Обогащает уран.")'),
        (r'createSimpleMachineItem\("MASS_FABRICATOR", Material.DROPPER, ".*?", ".*?"\)', r'createSimpleMachineItem("MASS_FABRICATOR", Material.DROPPER, "§dГенератор Материи", "Создает алмазы из мусора и энергии.")'),
        (r'createSimpleMachineItem\("ENERGY_STORAGE", Material.BLAST_FURNACE, ".*?", ".*?"\)', r'createSimpleMachineItem("ENERGY_STORAGE", Material.BLAST_FURNACE, "§aМФСУ", "Хранит до 10 000 000 EU.")'),
        (r'meta\.setDisplayName\(".*?"\);\s*meta\.setLore\(List\.of\(.*?\)\);', r'meta.setDisplayName("§bНано-Джетпак");\n        meta.setLore(List.of("§7Позволяет летать.", "§7Тратит энергию при полете.", "", "§eЭнергия: §a0 §7/ §a10000 EU"));')
    ],
    'UraniumItems.java': [
        (r'meta\.setDisplayName\("A 2D.*?"\);\s*meta\.setLore\(List\.of\(.*?\)\);', r'meta.setDisplayName("§2Необработанный Уран");\n        meta.setLore(List.of("§7Радиоактивная руда.", "§7Нужно обогащение в Центрифуге."));'),
        (r'meta\.setDisplayName\("A aD~D.*?"\);\s*meta\.setLore\(List\.of\(.*?\)\);', r'meta.setDisplayName("§aИзотоп Урана-235");\n        meta.setLore(List.of("§7Высокообогащенный уран.", "§7Используется для создания топливных стержней."));'),
        (r'meta\.setDisplayName\("A aD_D.*?"\);\s*meta\.setLore\(List\.of\(.*?\)\);', r'meta.setDisplayName("§aТопливный Стержень");\n        meta.setLore(List.of("§7Топливо для ядерного реактора."));'),
        (r'meta\.setDisplayName\("A 8Dz.*?"\);\s*meta\.setLore\(List\.of\(.*?\)\);', r'meta.setDisplayName("§8Отработанный Стержень");\n        meta.setLore(List.of("§cУГРОЗА: Высокая радиация!", "§7Подлежит утилизации."));'),
        (r'meta\.setDisplayName\("A 2D_D.*?"\);\s*meta\.setLore\(List\.of\(.*?\)\);', r'meta.setDisplayName("§2Токсичные отходы");\n        meta.setLore(List.of("§cУГРОЗА: Побочный продукт обогащения.", "§7Наносят сильнейший урон."));'),
        (r'meta\.setDisplayName\("A e" \+ name\);\s*meta\.setColor\(.*?\);\s*meta\.setLore\(List\.of\(.*?\)\);', r'meta.setDisplayName("§e" + name);\n        meta.setColor(Color.fromRGB(220, 220, 0));\n        meta.setLore(List.of("§7Защищает от радиации.", "§cПолный сет блокирует весь урон от радиации."));')
    ],
    'EnergyStorageWorker.java': [
        (r'java\.util\.List<Component> lore = meta\.lore\(\);.*?item\.setItemMeta\(meta\);', r'''java.util.List<Component> oldLore = meta.lore();
                            if (oldLore != null && oldLore.size() >= 4) {
                                java.util.List<Component> lore = new java.util.ArrayList<>(oldLore);
                                lore.set(3, LegacyComponentSerializer.legacySection().deserialize("§eЭнергия: §a" + itemEnergy + " §7/ §a" + maxItemEnergy + " EU"));
                                meta.lore(lore);
                            }
                            item.setItemMeta(meta);
                            container.getInventory().setItem(0, item);''')
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