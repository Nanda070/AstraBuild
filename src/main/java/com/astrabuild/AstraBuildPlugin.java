package com.astrabuild;

import com.astrabuild.events.EventManager;
import com.astrabuild.item.RecipeRegistry;
import com.astrabuild.listener.BlockListener;
import com.astrabuild.listener.ChunkScanner;
import com.astrabuild.listener.GuiListener;
import com.astrabuild.network.DeliveryService;
import com.astrabuild.network.PipeNetwork;
import com.astrabuild.quarry.QuarryManager;
import com.astrabuild.storage.MachineDataStorage;
import com.astrabuild.strike.OrbitalStrikeListener;
import com.astrabuild.strike.OrbitalStrikeManager;
import com.astrabuild.strike.RadiationManager;
import com.astrabuild.command.AstraBuildCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class AstraBuildPlugin extends JavaPlugin {

    private PipeNetwork pipeNetwork;
    private QuarryManager quarryManager;
    private MachineDataStorage machineDataStorage;
    private DeliveryService deliveryService;
    private RecipeRegistry recipeRegistry;
    private EventManager eventManager;
    private com.astrabuild.machine.IndustrialManager industrialManager;
    private com.astrabuild.strike.RadiationManager radiationManager;
    private com.astrabuild.visual.HologramManager hologramManager;

    private static AstraBuildPlugin instance;

    public static AstraBuildPlugin getInstance() {
        return instance;
    }

    public com.astrabuild.strike.RadiationManager getRadiationManager() {
        return radiationManager;
    }

    public com.astrabuild.visual.HologramManager getHologramManager() {
        return hologramManager;
    }

    public RecipeRegistry getRecipeRegistry() {
        return recipeRegistry;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // Очистка брошенных голограмм от прошлых сессий
        for (org.bukkit.World world : org.bukkit.Bukkit.getWorlds()) {
            for (org.bukkit.entity.TextDisplay display : world.getEntitiesByClass(org.bukkit.entity.TextDisplay.class)) {
                if (display.getPersistentDataContainer().has(new org.bukkit.NamespacedKey(this, "astrabuild_holo"), org.bukkit.persistence.PersistentDataType.BYTE)) {
                    display.remove();
                }
            }
        }

        // Инициализация слоя данных и логистики
        this.machineDataStorage = new MachineDataStorage(this);
        this.deliveryService = new DeliveryService(this, machineDataStorage);
        this.pipeNetwork = new PipeNetwork();
        
        // Инициализация EventManager
        this.eventManager = new EventManager(this);
        
        // Инициализация оркестраторов механизмов
        this.quarryManager = new QuarryManager(this, pipeNetwork, deliveryService, machineDataStorage, eventManager);
        this.recipeRegistry = new RecipeRegistry(this);
        this.radiationManager = new RadiationManager(this, recipeRegistry);
        this.hologramManager = new com.astrabuild.visual.HologramManager(this);
        this.industrialManager = new com.astrabuild.machine.IndustrialManager(this, machineDataStorage, deliveryService, pipeNetwork, recipeRegistry);
        this.recipeRegistry.registerAll();
        this.recipeRegistry.initUranium(this);
        this.recipeRegistry.registerUraniumRecipes();

        OrbitalStrikeManager strikeManager = new OrbitalStrikeManager(this);

        // Регистрация слушателей
        getServer().getPluginManager().registerEvents(new BlockListener(pipeNetwork, quarryManager, machineDataStorage, recipeRegistry, industrialManager), this);
        getServer().getPluginManager().registerEvents(new ChunkScanner(pipeNetwork, quarryManager, machineDataStorage, industrialManager), this);
        getServer().getPluginManager().registerEvents(new GuiListener(this, machineDataStorage, eventManager, recipeRegistry), this);
        getServer().getPluginManager().registerEvents(new OrbitalStrikeListener(this, strikeManager, recipeRegistry), this);
        getServer().getPluginManager().registerEvents(new com.astrabuild.listener.NanoEquipmentListener(this), this);
        
        AstraBuildCommand cmd = new AstraBuildCommand(recipeRegistry);
        getCommand("astrabuild").setExecutor(cmd);
        getCommand("astrabuild").setTabCompleter(cmd);
        
        getLogger().info("AstraBuild initialized. Logistics and Industrial systems online.");
    }

    @Override
    public void onDisable() {
        com.astrabuild.visual.DisplayUtil.cleanup();
        if (hologramManager != null) {
            hologramManager.cleanupAll();
        }
        if (quarryManager != null) {
            quarryManager.shutdownAll();
        }
        if (industrialManager != null) {
            industrialManager.shutdown();
        }
        getLogger().info("AstraBuild halted.");
    }

    public PipeNetwork getPipeNetwork() {
        return pipeNetwork;
    }

    public com.astrabuild.machine.IndustrialManager getIndustrialManager() {
        return industrialManager;
    }
}