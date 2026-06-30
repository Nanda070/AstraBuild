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

    private static final int ENERGY_PER_DIAMOND = 1000000; // ÐžÑ‡ÐµÐ½ÑŒ Ð´Ð¾Ñ€Ð¾Ð³Ð¾
    
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
                controllerLoc.getWorld().dropItemNaturally(controllerLoc.clone().add(0, 1, 0), item);
            }
        }
    }

    public void runTick() {
        Block block = controllerLoc.getBlock();
        
        // Ð•ÑÐ»Ð¸ ÑÐ½ÐµÑ€Ð³Ð¸Ð¸ Ð¼ÐµÐ½ÑŒÑˆÐµ, Ñ‡ÐµÐ¼ Ð½ÑƒÐ¶Ð½Ð¾ Ð½Ð° Ð¾Ð¿ÐµÑ€Ð°Ñ†Ð¸ÑŽ - Ð²Ñ‹Ñ…Ð¾Ð´Ð¸Ð¼
        if (storage.getEnergy(block) < ENERGY_PER_DIAMOND) {
            return;
        }

        // ÐŸÑ€Ð¾Ð²ÐµÑ€ÑÐµÐ¼ Ð¸Ð½Ð²ÐµÐ½Ñ‚Ð°Ñ€ÑŒ Ñ„Ð°Ð±Ñ€Ð¸ÐºÐ°Ñ‚Ð¾Ñ€Ð° Ð½Ð° Ð½Ð°Ð»Ð¸Ñ‡Ð¸Ðµ Ð¼ÑƒÑÐ¾Ñ€Ð°
        if (block.getState() instanceof Container container) {
            Inventory inv = container.getInventory();
            boolean hasScrap = false;
            
            ItemStack inputItem = inv.getItem(0);
            if (inputItem != null && SCRAP_MATERIALS.contains(inputItem.getType())) {
                inputItem.setAmount(inputItem.getAmount() - 1);
                hasScrap = true;
            }
            
            if (hasScrap) {
                // ÐŸÐ¾Ñ‚Ñ€ÐµÐ±Ð»ÑÐµÐ¼ ÑÐ½ÐµÑ€Ð³Ð¸ÑŽ
                if (storage.consumeEnergy(block, ENERGY_PER_DIAMOND)) {
                    // ÐŸÑ€Ð¾Ð¸Ð·Ð²Ð¾Ð´Ð¸Ð¼ Ð°Ð»Ð¼Ð°Ð·
                    ItemStack output = new ItemStack(Material.DIAMOND, 1);
                    
                    outputToSpecificSlot(output, inv, 1);
                } else {
                    if (inputItem != null) {
                        inputItem.setAmount(inputItem.getAmount() + 1);
                    }
                }
            }
        }
    }
}
