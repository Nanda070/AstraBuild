package com.astrabuild;

import com.astrabuild.item.RecipeRegistry;
import com.astrabuild.listener.BlockListener;
import com.astrabuild.listener.ChunkScanner;
import com.astrabuild.listener.GuiListener;
import com.astrabuild.network.DeliveryService;
import com.astrabuild.network.PipeNetwork;
import com.astrabuild.quarry.QuarryManager;
import com.astrabuild.storage.MachineDataStorage;
import org.bukkit.plugin.java.JavaPlugin;

public class AstraBuildPlugin extends JavaPlugin {

    private PipeNetwork pipeNetwork;
    private QuarryManager quarryManager;
    private MachineDataStorage machineDataStorage;
    private DeliveryService deliveryService;
    private RecipeRegistry recipeRegistry;

    @Override
    public void onEnable() {
        // Инициализация слоя данных и логистики
        this.machineDataStorage = new MachineDataStorage(this);
        this.deliveryService = new DeliveryService(this);
        this.pipeNetwork = new PipeNetwork();
        
        // Инициализация оркестратора механизмов
        this.quarryManager = new QuarryManager(this, pipeNetwork, deliveryService, machineDataStorage);
        
        // Регистрация крафтов
        this.recipeRegistry = new RecipeRegistry(this);
        this.recipeRegistry.registerAll();

        // Регистрация слушателей
        getServer().getPluginManager().registerEvents(new BlockListener(pipeNetwork, quarryManager, machineDataStorage, recipeRegistry), this);
        getServer().getPluginManager().registerEvents(new ChunkScanner(pipeNetwork, quarryManager, machineDataStorage), this);
        getServer().getPluginManager().registerEvents(new com.astrabuild.listener.GuiListener(this, machineDataStorage), this);
        
        getLogger().info("AstraBuild initialized. Logistics and Industrial systems online.");
    }

    @Override
    public void onDisable() {
        if (quarryManager != null) {
            quarryManager.shutdownAll();
        }
        getLogger().info("AstraBuild halted.");
    }

    public PipeNetwork getPipeNetwork() {
        return pipeNetwork;
    }
}