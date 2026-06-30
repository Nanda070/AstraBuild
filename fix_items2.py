import os
import re

files_to_fix = [
    r'src\main\java\com\astrabuild\item\RecipeRegistry.java',
    r'src\main\java\com\astrabuild\item\UraniumItems.java'
]

replacements = {
    'RecipeRegistry.java': [
        (r'createSimpleMachineItem\("CENTRIFUGE", Material\.DISPENSER, ".*?", ".*?"\)', r'createSimpleMachineItem("CENTRIFUGE", Material.DISPENSER, "§eЦентрифуга", "Обогащает уран.")'),
        (r'createSimpleMachineItem\("MASS_FABRICATOR", Material\.DROPPER, ".*?", ".*?"\)', r'createSimpleMachineItem("MASS_FABRICATOR", Material.DROPPER, "§dГенератор Материи", "Создает алмазы из мусора и энергии.")'),
        (r'createSimpleMachineItem\("ENERGY_STORAGE", Material\.BLAST_FURNACE, ".*?", ".*?"\)', r'createSimpleMachineItem("ENERGY_STORAGE", Material.BLAST_FURNACE, "§aМФСУ", "Хранит до 10 000 000 EU.")'),
        (r'meta\.setDisplayName\("A bD.*?D.*?D.*?D.*?D.*?D.*?D.*?"\);\n\s*meta\.setLore\(List\.of\(".*?", ".*?", "", ".*?"\)\);', r'meta.setDisplayName("§bНано-Джетпак");\n        meta.setLore(List.of("§7Позволяет летать.", "§7Тратит энергию при полете.", "", "§eЭнергия: §a0 §7/ §a10000 EU"));'),
        (r'meta\.setDisplayName\("A bD.*?D.*?D.*?"\);\n\s*meta\.setLore\(List\.of\(".*?", ".*?", "", ".*?"\)\);', r'meta.setDisplayName("§bНано-Меч");\n        meta.setLore(List.of("§7Убийственное оружие.", "§7Тратит энергию при ударе.", "", "§eЭнергия: §a0 §7/ §a5000 EU"));')
    ],
    'UraniumItems.java': [
        (r'meta\.setDisplayName\("A 2D.*?"\);\n\s*meta\.setLore\(List\.of\(".*?", ".*?"\)\);', r'meta.setDisplayName("§2Необработанный Уран");\n        meta.setLore(List.of("§7Радиоактивная руда.", "§7Нужно обогащение в Центрифуге."));'),
        (r'meta\.setDisplayName\("A aD~D.*?"\);\n\s*meta\.setLore\(List\.of\(".*?", ".*?"\)\);', r'meta.setDisplayName("§aИзотоп Урана-235");\n        meta.setLore(List.of("§7Высокообогащенный уран.", "§7Используется для создания топливных стержней."));'),
        (r'meta\.setDisplayName\("A aD_D.*?"\);\n\s*meta\.setLore\(List\.of\(".*?"\)\);', r'meta.setDisplayName("§aТопливный Стержень");\n        meta.setLore(List.of("§7Топливо для ядерного реактора."));'),
        (r'meta\.setDisplayName\("A 8Dz.*?"\);\n\s*meta\.setLore\(List\.of\(".*?", ".*?"\)\);', r'meta.setDisplayName("§8Отработанный Стержень");\n        meta.setLore(List.of("§cУГРОЗА: Высокая радиация!", "§7Подлежит утилизации."));'),
        (r'meta\.setDisplayName\("A 2D_D.*?"\);\n\s*meta\.setLore\(List\.of\(".*?", ".*?"\)\);', r'meta.setDisplayName("§2Токсичные отходы");\n        meta.setLore(List.of("§cУГРОЗА: Побочный продукт обогащения.", "§7Наносят сильнейший урон."));'),
        (r'meta\.setDisplayName\("A e" \+ name\);\n\s*meta\.setColor\(.*?\);\n\s*meta\.setLore\(List\.of\(".*?", ".*?"\)\);', r'meta.setDisplayName("§e" + name);\n        meta.setColor(Color.fromRGB(220, 220, 0));\n        meta.setLore(List.of("§7Защищает от радиации.", "§cПолный сет блокирует весь урон от радиации."));')
    ]
}

for file_path in files_to_fix:
    if os.path.exists(file_path):
        with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
            content = f.read()
        
        filename = os.path.basename(file_path)
        if filename in replacements:
            for pattern, repl in replacements[filename]:
                content = re.sub(pattern, repl, content)
        
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Fixed {filename}")
    else:
        print(f"File not found: {file_path}")