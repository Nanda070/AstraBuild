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
            
            // Берем уран ТОЛЬКО из 0 слота (Input)
            ItemStack inputItem = inv.getItem(0);
            if (inputItem != null && "URANIUM_RAW".equals(recipeRegistry.getMachineItemType(inputItem))) {
                inputItem.setAmount(inputItem.getAmount() - 1);
                hasRawUranium = true;
            }
            
            if (hasRawUranium) {
                if (storage.consumeEnergy(block, ENERGY_PER_OPERATION)) {
                    ItemStack isotope = recipeRegistry.getUraniumItems().getItem("URANIUM_ISOTOPE");
                    // Кладем выход в 1 слот
                    outputToSpecificSlot(isotope, inv, 1);
                    
                    // Шанс 10% на токсичные отходы - во 2 слот
                    if (random.nextDouble() < 0.1) {
                        ItemStack waste = recipeRegistry.getUraniumItems().getItem("TOXIC_WASTE");
                        outputToSpecificSlot(waste, inv, 2);
                    }
                } else {
                    // Если энергия закончилась прямо перед крафтом, возвращаем предмет
                    if (inputItem != null) {
                        inputItem.setAmount(inputItem.getAmount() + 1);
                    }
                }
            }
        }
    }
    
    private void outputToSpecificSlot(ItemStack item, Inventory inv, int slot) {
        PipeNetwork.PathResult pathResult = network.findShortestPath(controllerLoc, com.astrabuild.model.NetworkNode.NodeType.PIPE_ENERGY);
        if (pathResult != null && !pathResult.path().isEmpty()) {
            deliveryService.deliverItem(pathResult.destination(), item);
        } else {
            ItemStack existing = inv.getItem(slot);
            if (existing == null || existing.getType().isAir()) {
                inv.setItem(slot, item);
            } else if (existing.isSimilar(item) && existing.getAmount() + item.getAmount() <= existing.getMaxStackSize()) {
                existing.setAmount(existing.getAmount() + item.getAmount());
            } else {
                // Если слот забит другим предметом или переполнен, выбрасываем в мир
                controllerLoc.getWorld().dropItemNaturally(controllerLoc.clone().add(0, 1, 0), item);
            }
        }
    }
}
