package com.astrabuild.listener;

import com.astrabuild.item.RecipeRegistry;
import com.astrabuild.model.NetworkNode;
import com.astrabuild.network.PipeNetwork;
import com.astrabuild.quarry.QuarryManager;
import com.astrabuild.storage.MachineDataStorage;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public class BlockListener implements Listener {

    private final PipeNetwork network;
    private final QuarryManager quarryManager;
    private final MachineDataStorage storage;
    private final RecipeRegistry recipeRegistry;
    private final com.astrabuild.machine.IndustrialManager industrialManager;

    public BlockListener(PipeNetwork network, QuarryManager quarryManager, MachineDataStorage storage, RecipeRegistry recipeRegistry, com.astrabuild.machine.IndustrialManager industrialManager) {
        this.network = network;
        this.quarryManager = quarryManager;
        this.storage = storage;
        this.recipeRegistry = recipeRegistry;
        this.industrialManager = industrialManager;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        String machineType = recipeRegistry.getMachineItemType(item);

        if (event.getBlock().getType().name().endsWith("_ORE")) {
            org.bukkit.plugin.Plugin plugin = com.astrabuild.AstraBuildPlugin.getInstance();
            event.getBlock().setMetadata("astrabuild_player_placed", new org.bukkit.metadata.FixedMetadataValue(plugin, true));
        }

        if (machineType == null) return;

        Location loc = event.getBlock().getLocation();
        
        // Пишем тип механизма в блок
        storage.setMachineType(event.getBlock(), machineType);
        
        switch (machineType) {
            case "QUARRY" -> {
                org.bukkit.block.Block block = event.getBlock();
                org.bukkit.block.BlockFace face = org.bukkit.block.BlockFace.NORTH;
                
                if (block.getBlockData() instanceof org.bukkit.block.data.Directional dir) {
                    face = dir.getFacing();
                }

                org.bukkit.block.Block frontBlock = block.getRelative(face);
                org.bukkit.Chunk targetChunk = frontBlock.getChunk();
                
                int startX = targetChunk.getX() * 16;
                int startZ = targetChunk.getZ() * 16;
                int maxY = block.getY() - 1;
                int minY = block.getWorld().getMinHeight();

                Location p1 = new Location(block.getWorld(), startX, maxY, startZ);
                Location p2 = new Location(block.getWorld(), startX + 15, minY, startZ + 15);
                
                storage.setQuarryBounds(block, p1, p2);
                quarryManager.createAndStart(block.getLocation(), p1, p2);
                network.registerNode(block.getLocation(), com.astrabuild.model.NetworkNode.NodeType.MACHINE_QUARRY);
            }
            case "PIPE" -> network.registerNode(loc, NetworkNode.NodeType.PIPE);
            case "CONNECTOR_INSERT" -> network.registerNode(loc, NetworkNode.NodeType.CONNECTOR_INSERT);
            case "SATELLITE_TERMINAL" -> network.registerNode(loc, NetworkNode.NodeType.MACHINE_SATELLITE);
            // Новые механизмы Эпохи Атома
            case "REACTOR_CORE", "CENTRIFUGE", "MASS_FABRICATOR", "ENERGY_STORAGE", "COOLANT_TANK" -> {
                if (industrialManager != null) {
                    industrialManager.registerMachine(loc, machineType);
                }
                // Также регистрируем их в сети труб (чтобы находить)
                network.registerNode(loc, NetworkNode.NodeType.MACHINE_SATELLITE); // Или другой тип, пока используем этот как базовый для механизмов
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        String machineType = storage.getMachineType(event.getBlock());
        
        if (machineType != null) {
            Location loc = event.getBlock().getLocation();
            
            if (machineType.equals("QUARRY")) {
                quarryManager.removeQuarry(loc);
            } else if (machineType.equals("REACTOR_CORE") || machineType.equals("CENTRIFUGE") || 
                       machineType.equals("MASS_FABRICATOR") || machineType.equals("ENERGY_STORAGE") ||
                       machineType.equals("COOLANT_TANK")) {
                if (industrialManager != null) {
                    industrialManager.unregisterMachine(loc);
                }
            }
            
            network.unregisterNode(loc);
            storage.clearMachineData(event.getBlock());
            
            // Если это контейнер, вручную выбрасываем его содержимое, так как setDropItems(false) удалит и инвентарь
            if (event.getBlock().getState() instanceof org.bukkit.block.Container container) {
                for (ItemStack invItem : container.getInventory().getContents()) {
                    if (invItem != null && !invItem.getType().isAir()) {
                        loc.getWorld().dropItemNaturally(loc, invItem);
                    }
                }
                container.getInventory().clear();
            }

            // Кастомный дроп механизма
            event.setDropItems(false); // Отключаем ванильный лут (выпадение обычного стекла/печи/раздатчика)
            
            ItemStack customDrop = recipeRegistry.getMachineItem(machineType);
            if (customDrop != null) {
                loc.getWorld().dropItemNaturally(loc, customDrop);
            }
        }
    }
}