import os

file_path = r'src\main\java\com\astrabuild\machine\NuclearReactorCore.java'

if os.path.exists(file_path):
    with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
        lines = f.readlines()
    
    for i in range(len(lines)):
        if 'if (!isIntact) {' in lines[i]:
            lines[i+1] = '            hm.updateHologram(coreLocation, "§c[РЕАКТОР СЛОМАН]\\n§7Нужна база 3x3\\n§7и 4 железных столба");\n'
        if 'String heatColor = heat < 3000' in lines[i]:
            lines[i+2] = '        String text = "§b⚡ Ядерный Реактор\\n" + heatColor + "🔥 Температура: " + heat + " / 10000\\n" + "§e⚡ Энергия: " + energy + " / " + max;\n'
    
    with open(file_path, 'w', encoding='utf-8') as f:
        f.writelines(lines)
    print("Fixed NuclearReactorCore.java")