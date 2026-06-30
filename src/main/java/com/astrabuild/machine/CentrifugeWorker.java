package com.astrabuild.machine;

import com.astrabuild.item.RecipeRegistry;
import com.astrabuild.network.DeliveryService;
import com.astrabuild.network.PipeNetwork;
import com.astrabuild.storage.MachineDataStorage;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class CentrifugeWorker {

    private final Location controllerLoc;
    private final MachineDataStorage storage;
    private final DeliveryService deliveryService;
    private final PipeNetwork network;
    private final RecipeRegistry recipeRegistry;
    
    private final Random random = new Random();
    private static final int ENERGY_PER_OPERATION = 500;

    public CentrifugeWorker(Location controllerLoc, MachineDataStorage storage, DeliveryService deliveryService, PipeNetwork network, RecipeRegistry recipeRegistry) {
        this.controllerLoc = controllerLoc;
        this.storage = storage;
        this.deliveryService = deliveryService;
        this.network = network;
        this.recipeRegistry = recipeRegistry;
    }

    public void runTick() {
        Block block = controllerLoc.getBlock();
        
        if (storage.getEnergy(block) < ENERGY_PER_OPERATION) {
            return;
        }

        if (block.getState() instanceof Container container) {
            Inventory inv = container.getInventory();
            boolean hasRawUranium = false;
            
            for (int i = 0; i < inv.getSize(); i++) {
                ItemStack item = inv.getItem(i);
                if (item != null && "URANIUM_RAW".equals(recipeRegistry.getMachineItemType(item))) {
                    item.setAmount(item.getAmount() - 1);
                    hasRawUranium = true;
                    break;
                }
            }
            
            if (hasRawUranium) {
                if (storage.consumeEnergy(block, ENERGY_PER_OPERATION)) {
                    ItemStack isotope = recipeRegistry.getUraniumItems().getItem("URANIUM_ISOTOPE");
                    outputItem(isotope, inv);
                    
                    // Шанс 10% на токсичные отходы
                    if (random.nextDouble() < 0.1) {
                        ItemStack waste = recipeRegistry.getUraniumItems().getItem("TOXIC_WASTE");
                        outputItem(waste, inv);
                    }
                }
            }
        }
    }
    
    private void outputItem(ItemStack item, Inventory inv) {
        PipeNetwork.PathResult pathResult = network.findShortestPath(controllerLoc);
        if (pathResult != null && !pathResult.path().isEmpty()) {
            deliveryService.deliverItem(pathResult.destination(), item);
        } else {
            inv.addItem(item).forEach((index, leftover) -> {
                controllerLoc.getWorld().dropItemNaturally(controllerLoc.clone().add(0, 1, 0), leftover);
            });
        }
    }
}
