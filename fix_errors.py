import os

replacements = {
    r'src\main\java\com\astrabuild\listener\BlockListener.java': [
        ('cleanArea(loc, 100);', 'cleanArea(event.getBlock().getLocation(), 100);'),
        ('loc.getWorld().spawnParticle', 'event.getBlock().getLocation().getWorld().spawnParticle'),
        ('loc.getWorld().playSound', 'event.getBlock().getLocation().getWorld().playSound'),
        ('Particle.END_ROD, loc, 500', 'Particle.END_ROD, event.getBlock().getLocation(), 500'),
        ('playSound(loc, org', 'playSound(event.getBlock().getLocation(), org')
    ],
    r'src\main\java\com\astrabuild\machine\NuclearReactorCore.java': [
        ('network.getConnectedNodes(coreLocation)', 'network.getConnectedNodes(coreLocation, com.astrabuild.model.NetworkNode.NodeType.PIPE_ENERGY)')
    ],
    r'src\main\java\com\astrabuild\machine\CentrifugeWorker.java': [
        ('network.findShortestPath(controllerLoc)', 'network.findShortestPath(controllerLoc, com.astrabuild.model.NetworkNode.NodeType.PIPE_ENERGY)')
    ],
    r'src\main\java\com\astrabuild\machine\MassFabricatorWorker.java': [
        ('network.findShortestPath(controllerLoc)', 'network.findShortestPath(controllerLoc, com.astrabuild.model.NetworkNode.NodeType.PIPE_ENERGY)')
    ],
    r'src\main\java\com\astrabuild\machine\EnergyStorageWorker.java': [
        ('getConnectedNodes(controllerLoc)', 'getConnectedNodes(controllerLoc, com.astrabuild.model.NetworkNode.NodeType.PIPE_ENERGY)')
    ],
    r'src\main\java\com\astrabuild\quarry\QuarryWorker.java': [
        ('network.findShortestPath(controllerLoc)', 'network.findShortestPath(controllerLoc, com.astrabuild.model.NetworkNode.NodeType.PIPE_ITEM)')
    ],
    r'src\main\java\com\astrabuild\listener\ChunkScanner.java': [
        ('case "PIPE" -> network.registerNode(loc, NetworkNode.NodeType.PIPE);', 'case "ENERGY_PIPE" -> network.registerNode(loc, NetworkNode.NodeType.PIPE_ENERGY);\n                case "ITEM_PIPE" -> network.registerNode(loc, NetworkNode.NodeType.PIPE_ITEM);'),
        ('case "QUARRY", "PIPE",', 'case "QUARRY", "ENERGY_PIPE", "ITEM_PIPE",')
    ]
}

for file_path, changes in replacements.items():
    if os.path.exists(file_path):
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        for old, new in changes:
            content = content.replace(old, new)
            
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(content)

print("Fixed errors.")