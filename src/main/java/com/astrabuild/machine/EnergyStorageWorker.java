package com.astrabuild.machine;

import com.astrabuild.storage.MachineDataStorage;
import org.bukkit.Location;
import org.bukkit.block.Block;

public class EnergyStorageWorker {

    private final Location controllerLoc;
    private final MachineDataStorage storage;
    
    // МФСУ хранит до 10 000 000 энергии
    public static final int MAX_ENERGY = 10000000;

    public EnergyStorageWorker(Location controllerLoc, MachineDataStorage storage) {
        this.controllerLoc = controllerLoc;
        this.storage = storage;
    }

    private int tickCounter = 0;

    public void runTick() {
        Block block = controllerLoc.getBlock();
        int currentEnergy = storage.getEnergy(block);
        
        // Лимитируем максимальную емкость, если превышает
        if (currentEnergy > MAX_ENERGY) {
            storage.consumeEnergy(block, currentEnergy - MAX_ENERGY);
            currentEnergy = MAX_ENERGY;
        }

        tickCounter++;
        
        // Обновление голограммы
        if (tickCounter >= 20) {
            com.astrabuild.visual.HologramManager hm = com.astrabuild.AstraBuildPlugin.getInstance().getHologramManager();
            if (hm != null) {
                int energy = storage.getEnergy(block);
                hm.updateHologram(controllerLoc, "§a⚡ МФСУ\n§e" + energy + " / " + MAX_ENERGY + " EU");
            }
        }
        
        // Раздача энергии в сеть (до 10,000 EU за раз, раз в секунду)
        if (tickCounter >= 20) {
            tickCounter = 0;
            if (currentEnergy > 0) {
                java.util.Set<Location> connectedNodes = com.astrabuild.AstraBuildPlugin.getInstance().getPipeNetwork().getConnectedNodes(controllerLoc);
                int distributed = 0;
                int maxOutput = 500 * 20; // 10000 EU per second
                
                for (Location loc : connectedNodes) {
                    if (distributed >= maxOutput || distributed >= currentEnergy) break;
                    Block targetBlock = loc.getBlock();
                    String targetType = storage.getMachineType(targetBlock);
                    
                    // Не питаем Реакторы и другие Батареи (чтобы не было бесконечного цикла)
                    if (targetType != null && !targetType.equals("REACTOR_CORE") && !targetType.equals("ENERGY_STORAGE")) {
                        int targetEnergy = storage.getEnergy(targetBlock);
                        int maxTargetEnergy = storage.getMaxEnergy(targetType);
                        int canTake = maxTargetEnergy - targetEnergy;
                        
                        if (canTake > 0) {
                            int toGive = Math.min(maxOutput - distributed, Math.min(currentEnergy - distributed, canTake));
                            storage.addEnergy(targetBlock, toGive);
                            distributed += toGive;
                        }
                    }
                }
                
                if (distributed > 0) {
                    storage.consumeEnergy(block, distributed);
                }
            }
        }
    }
    
    public Location getLocation() {
        return controllerLoc;
    }
}
