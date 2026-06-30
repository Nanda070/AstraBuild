package com.astrabuild.machine;

import com.astrabuild.storage.MachineDataStorage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.block.Block;

public class EnergyStorageWorker {

    private final Location controllerLoc;
    private final MachineDataStorage storage;
    
    public static final int MAX_ENERGY = 10000000;

    private static final org.bukkit.NamespacedKey ENERGY_KEY =
            new org.bukkit.NamespacedKey(com.astrabuild.AstraBuildPlugin.getInstance(), "astrabuild_energy");

    public EnergyStorageWorker(Location controllerLoc, MachineDataStorage storage) {
        this.controllerLoc = controllerLoc;
        this.storage = storage;
    }

    private int tickCounter = 0;

    public void runTick() {
        Block block = controllerLoc.getBlock();
        int currentEnergy = storage.getEnergy(block);
        
        if (currentEnergy > MAX_ENERGY) {
            storage.consumeEnergy(block, currentEnergy - MAX_ENERGY);
            currentEnergy = MAX_ENERGY;
        }

        tickCounter++;
        
        if (currentEnergy > 0 && block.getState() instanceof org.bukkit.block.Container container) {
            org.bukkit.inventory.ItemStack item = container.getInventory().getItem(0);
            if (item != null && item.hasItemMeta()) {
                com.astrabuild.item.RecipeRegistry recipeRegistry = com.astrabuild.AstraBuildPlugin.getInstance().getRecipeRegistry();
                if (recipeRegistry != null) {
                    String handType = recipeRegistry.getMachineItemType(item);
                    if ("NANO_CHESTPLATE".equals(handType) || "NANO_SWORD".equals(handType)) {
                        org.bukkit.persistence.PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
                        Integer itemEnergy = pdc.get(ENERGY_KEY, org.bukkit.persistence.PersistentDataType.INTEGER);
                        if (itemEnergy == null) itemEnergy = 0;
                        
                        int maxItemEnergy = "NANO_CHESTPLATE".equals(handType) ? 10000 : 5000;
                        int needed = maxItemEnergy - itemEnergy;
                        
                        if (needed > 0) {
                            int toTransfer = Math.min(needed, Math.min(currentEnergy, 50)); 
                            storage.consumeEnergy(block, toTransfer);
                            currentEnergy -= toTransfer;
                            itemEnergy += toTransfer;
                            
                            org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
                            meta.getPersistentDataContainer().set(ENERGY_KEY, org.bukkit.persistence.PersistentDataType.INTEGER, itemEnergy);
                            java.util.List<Component> oldLore = meta.lore();
                            if (oldLore != null && oldLore.size() >= 4) {
                                java.util.List<Component> lore = new java.util.ArrayList<>(oldLore);
                                lore.set(3, LegacyComponentSerializer.legacySection().deserialize("§eЭнергия: §a" + itemEnergy + " §7/ §a" + maxItemEnergy + " EU"));
                                meta.lore(lore);
                            }
                            item.setItemMeta(meta);
                            container.getInventory().setItem(0, item);
                        }
                    }
                }
            }
        }
        
        if (tickCounter >= 20) {
            com.astrabuild.visual.HologramManager hm = com.astrabuild.AstraBuildPlugin.getInstance().getHologramManager();
            if (hm != null) {
                int energy = storage.getEnergy(block);
                hm.updateHologram(controllerLoc, "§a⚡ МФСУ\n§e" + energy + " / " + MAX_ENERGY + " EU");
            }
        }
        
        if (tickCounter >= 20) {
            tickCounter = 0;
            if (currentEnergy > 0) {
                java.util.Set<Location> connectedNodes = com.astrabuild.AstraBuildPlugin.getInstance().getPipeNetwork().getConnectedNodes(controllerLoc, com.astrabuild.model.NetworkNode.NodeType.PIPE_ENERGY);
                int distributed = 0;
                int maxOutput = 500 * 20; 
                
                for (Location loc : connectedNodes) {
                    if (distributed >= maxOutput || distributed >= currentEnergy) break;
                    Block targetBlock = loc.getBlock();
                    String targetType = storage.getMachineType(targetBlock);
                    
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