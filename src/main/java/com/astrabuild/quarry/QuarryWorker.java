package com.astrabuild.quarry;

import com.astrabuild.network.DeliveryService;
import com.astrabuild.network.PipeNetwork;
import com.astrabuild.storage.MachineDataStorage;
import com.astrabuild.visual.DisplayUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

public class QuarryWorker {
    private final Plugin plugin;
    private final Location controllerLoc;
    private final PipeNetwork network;
    private final DeliveryService deliveryService;
    private final MachineDataStorage storage;
    private final Queue<Location> scanQueue = new LinkedList<>();
    
    private boolean active = true;
    private int cooldown = 0; // Задержка копки
    private final ItemStack fakePickaxe = new ItemStack(Material.DIAMOND_PICKAXE);

    public QuarryWorker(Plugin plugin, Location controllerLoc, PipeNetwork network, DeliveryService deliveryService, MachineDataStorage storage, Location p1, Location p2) {
        this.plugin = plugin;
        this.controllerLoc = controllerLoc;
        this.network = network;
        this.deliveryService = deliveryService;
        this.storage = storage;
        initializeQueue(p1, p2);
    }

    private void initializeQueue(Location p1, Location p2) {
        int minX = Math.min(p1.getBlockX(), p2.getBlockX());
        int maxX = Math.max(p1.getBlockX(), p2.getBlockX());
        int minY = Math.min(p1.getBlockY(), p2.getBlockY());
        int maxY = Math.max(p1.getBlockY(), p2.getBlockY());
        int minZ = Math.min(p1.getBlockZ(), p2.getBlockZ());
        int maxZ = Math.max(p1.getBlockZ(), p2.getBlockZ());

        for (int y = maxY; y >= minY; y--) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    scanQueue.add(new Location(p1.getWorld(), x, y, z));
                }
            }
        }
    }

    public boolean isActive() {
        return this.active;
    }

    public void runTick() {
        if (!active) return;
        
        if (cooldown > 0) {
            cooldown--;
            return;
        }

        Location targetLoc = null;
        Block block = null;

        // Мгновенный пропуск воздуха и бедрока без затрат тиков
        while (!scanQueue.isEmpty()) {
            targetLoc = scanQueue.peek();
            block = targetLoc.getBlock();
            if (block.getType().isAir() || block.getType() == Material.BEDROCK) {
                scanQueue.poll();
                block = null;
            } else {
                break;
            }
        }

        if (block == null) {
            active = false;
            return;
        }

        if (!storage.consumeEnergy(controllerLoc.getBlock(), 1)) {
            return;
        }

        scanQueue.poll();
        Collection<ItemStack> drops = block.getDrops(fakePickaxe);
        
        // Визуал ломания блока
        targetLoc.getWorld().spawnParticle(org.bukkit.Particle.BLOCK, targetLoc.clone().add(0.5, 0.5, 0.5), 15, block.getBlockData());
        block.setType(Material.AIR, true);
            
        if (!drops.isEmpty()) {
            pushToNetwork(drops);
        }

        // Задаем скорость алмазной кирки (6 тиков = 0.3 сек на блок)
        this.cooldown = 6; 
    }

    private void pushToNetwork(Collection<ItemStack> items) {
        PipeNetwork.PathResult pathResult = network.findShortestPath(controllerLoc);
        
        if (pathResult == null || pathResult.path().isEmpty()) {
            Location dropLoc = controllerLoc.clone().add(0, 1, 0);
            for (ItemStack item : items) {
                dropLoc.getWorld().dropItemNaturally(dropLoc, item);
            }
            return;
        }

        int ticksPerPipe = 5;

        for (ItemStack item : items) {
            DisplayUtil.animateItemPath(plugin, item, pathResult.path(), ticksPerPipe, () -> {
                deliveryService.deliverItem(pathResult.destination(), item);
            });
        }
    }

    public void halt() {
        this.active = false;
    }
}