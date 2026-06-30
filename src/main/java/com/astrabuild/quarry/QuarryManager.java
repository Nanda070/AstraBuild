package com.astrabuild.quarry;

import com.astrabuild.events.EventManager;
import com.astrabuild.network.DeliveryService;
import com.astrabuild.network.PipeNetwork;
import com.astrabuild.storage.MachineDataStorage;

import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class QuarryManager {
    private final Plugin plugin;
    private final PipeNetwork pipeNetwork;
    private final DeliveryService deliveryService;
    private final Map<Location, QuarryWorker> activeQuarries = new ConcurrentHashMap<>();
    private final BukkitTask tickerTask;
    private final MachineDataStorage storage;
    private final EventManager eventManager;

    public QuarryManager(Plugin plugin, PipeNetwork pipeNetwork, DeliveryService deliveryService, MachineDataStorage storage, EventManager eventManager) {
        this.plugin = plugin;
        this.pipeNetwork = pipeNetwork;
        this.deliveryService = deliveryService;
        this.storage = storage;
        this.eventManager = eventManager;

        // Изменено с 5L на 1L. Тикер работает каждый игровой тик.
        this.tickerTask = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tickAll, 20L, 1L);
        
        loadActiveQuarries();
    }

    public void createAndStart(Location controller, Location pos1, Location pos2) {
        if (activeQuarries.containsKey(controller)) return;

        QuarryWorker worker = new QuarryWorker(plugin, controller, pipeNetwork, deliveryService, storage, eventManager, pos1, pos2);
        activeQuarries.put(controller, worker);
        saveActiveQuarries();
    }

    public void removeQuarry(Location controller) {
        QuarryWorker worker = activeQuarries.remove(controller);
        if (worker != null) {
            worker.halt();
            saveActiveQuarries();
        }
    }

    private void saveActiveQuarries() {
        org.bukkit.configuration.file.YamlConfiguration config = new org.bukkit.configuration.file.YamlConfiguration();
        java.util.List<String> locs = new java.util.ArrayList<>();
        for (Location loc : activeQuarries.keySet()) {
            if (loc.getWorld() != null) {
                locs.add(loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ());
            }
        }
        config.set("quarries", locs);
        try {
            config.save(new java.io.File(plugin.getDataFolder(), "quarries.yml"));
        } catch (java.io.IOException ignored) {}
    }

    private void loadActiveQuarries() {
        java.io.File file = new java.io.File(plugin.getDataFolder(), "quarries.yml");
        if (!file.exists()) return;
        org.bukkit.configuration.file.YamlConfiguration config = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(file);
        for (String s : config.getStringList("quarries")) {
            String[] parts = s.split(",");
            if (parts.length == 4) {
                org.bukkit.World w = org.bukkit.Bukkit.getWorld(parts[0]);
                if (w != null) {
                    Location loc = new Location(w, Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
                    // Force load the chunk asynchronously or synchronously to trigger ChunkScanner
                    loc.getChunk().load(true);
                }
            }
        }
    }

    private void tickAll() {
        Iterator<Map.Entry<Location, QuarryWorker>> iterator = activeQuarries.entrySet().iterator();
        
        while (iterator.hasNext()) {
            QuarryWorker worker = iterator.next().getValue();
            
            if (!worker.isActive()) {
                iterator.remove();
                continue;
            }
            worker.runTick(); 
        }
    }

    public void shutdownAll() {
        if (tickerTask != null) tickerTask.cancel();
        activeQuarries.values().forEach(q -> { if (q != null) q.halt(); });
        activeQuarries.clear();
    }
}