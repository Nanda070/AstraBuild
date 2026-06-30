import os
import re

file_path = r'src\main\java\com\astrabuild\listener\BlockListener.java'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('case "PIPE" -> network.registerNode(loc, NetworkNode.NodeType.PIPE);', 'case "ENERGY_PIPE" -> network.registerNode(loc, NetworkNode.NodeType.PIPE_ENERGY);\n            case "ITEM_PIPE" -> network.registerNode(loc, NetworkNode.NodeType.PIPE_ITEM);')
content = content.replace('case "QUARRY", "PIPE",', 'case "QUARRY", "ENERGY_PIPE", "ITEM_PIPE",')

radiation_cleaner_logic = '''        if ("RADIATION_CLEANER".equals(machineType)) {
            com.astrabuild.AstraBuildPlugin.getInstance().getRadiationManager().cleanArea(loc, 100);
            event.getBlock().setType(org.bukkit.Material.AIR);
            loc.getWorld().spawnParticle(org.bukkit.Particle.END_ROD, loc, 500, 10, 10, 10, 0.1);
            loc.getWorld().playSound(loc, org.bukkit.Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 2.0f);
            return;
        }'''

content = content.replace('String machineType = recipeRegistry.getMachineItemType(item);', 'String machineType = recipeRegistry.getMachineItemType(item);\n\n' + radiation_cleaner_logic)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Updated BlockListener.java")