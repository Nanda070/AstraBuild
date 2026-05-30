package com.astrabuild.quarry;

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

    public QuarryManager(Plugin plugin, PipeNetwork pipeNetwork, DeliveryService deliveryService, MachineDataStorage storage) {
        this.plugin = plugin;
        this.pipeNetwork = pipeNetwork;
        this.deliveryService = deliveryService;
        this.storage = storage;

        // Изменено с 5L на 1L. Тикер работает каждый игровой тик.
        this.tickerTask = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tickAll, 20L, 1L);
    }

    public void createAndStart(Location controller, Location pos1, Location pos2) {
        if (activeQuarries.containsKey(controller)) return;

        QuarryWorker worker = new QuarryWorker(plugin, controller, pipeNetwork, deliveryService, storage, pos1, pos2);
        activeQuarries.put(controller, worker);
    }

    public void removeQuarry(Location controller) {
        QuarryWorker worker = activeQuarries.remove(controller);
        if (worker != null) {
            worker.halt();
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
        activeQuarries.values().forEach(QuarryWorker::halt);
        activeQuarries.clear();
    }
}