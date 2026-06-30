package com.astrabuild.machine;

import com.astrabuild.network.DeliveryService;
import com.astrabuild.network.PipeNetwork;
import com.astrabuild.storage.MachineDataStorage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.EnumSet;
import java.util.Set;

public class MassFabricatorWorker {

    private final Location controllerLoc;
    private final MachineDataStorage storage;
    private final DeliveryService deliveryService;
    private final PipeNetwork network;

    private static final int ENERGY_PER_DIAMOND = 100000; // Очень дорого
    
    private static final Set<Material> SCRAP_MATERIALS = EnumSet.of(
            Material.COBBLESTONE, Material.DIRT, Material.GRAVEL, 
            Material.ANDESITE, Material.DIORITE, Material.GRANITE,
            Material.SAND, Material.SANDSTONE
    );

    public MassFabricatorWorker(Location controllerLoc, MachineDataStorage storage, DeliveryService deliveryService, PipeNetwork network) {
        this.controllerLoc = controllerLoc;
        this.storage = storage;
        this.deliveryService = deliveryService;
        this.network = network;
    }

    public void runTick() {
        Block block = controllerLoc.getBlock();
        
        // Если энергии меньше, чем нужно на операцию - выходим
        if (storage.getEnergy(block) < ENERGY_PER_DIAMOND) {
            return;
        }

        // Проверяем инвентарь фабрикатора на наличие мусора
        if (block.getState() instanceof Container container) {
            Inventory inv = container.getInventory();
            boolean hasScrap = false;
            
            for (int i = 0; i < inv.getSize(); i++) {
                ItemStack item = inv.getItem(i);
                if (item != null && SCRAP_MATERIALS.contains(item.getType())) {
                    // Уничтожаем 1 единицу мусора (материи)
                    item.setAmount(item.getAmount() - 1);
                    hasScrap = true;
                    break;
                }
            }
            
            if (hasScrap) {
                // Списываем энергию
                if (storage.consumeEnergy(block, ENERGY_PER_DIAMOND)) {
                    // Производим алмаз
                    ItemStack output = new ItemStack(Material.DIAMOND, 1);
                    
                    // Пытаемся отправить алмаз в сеть
                    PipeNetwork.PathResult pathResult = network.findShortestPath(controllerLoc);
                    if (pathResult != null && !pathResult.path().isEmpty()) {
                        deliveryService.deliverItem(pathResult.destination(), output);
                    } else {
                        // Или просто кладем в сундук/выбрасываем наверх
                        inv.addItem(output).forEach((index, leftover) -> {
                            controllerLoc.getWorld().dropItemNaturally(controllerLoc.clone().add(0, 1, 0), leftover);
                        });
                    }
                }
            }
        }
    }
}
