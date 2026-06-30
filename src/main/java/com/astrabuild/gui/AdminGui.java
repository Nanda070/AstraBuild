package com.astrabuild.gui;

import com.astrabuild.item.RecipeRegistry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

@SuppressWarnings("deprecation")
public class AdminGui implements InventoryHolder {
    private final Inventory inventory;

    public AdminGui(RecipeRegistry recipeRegistry) {
        this.inventory = Bukkit.createInventory(this, 54, "§cАдмин-панель AstraBuild");
        
        // Список всех предметов для выдачи
        inventory.setItem(0, recipeRegistry.getMachineItem("QUARRY"));
        inventory.setItem(1, recipeRegistry.getMachineItem("PIPE"));
        inventory.setItem(2, recipeRegistry.getMachineItem("CONNECTOR_INSERT"));
        inventory.setItem(3, recipeRegistry.getMachineItem("UPGRADE_FORTUNE"));
        inventory.setItem(4, recipeRegistry.getMachineItem("UPGRADE_SILK_TOUCH"));
        inventory.setItem(5, recipeRegistry.getMachineItem("UPGRADE_OVERCLOCKER"));
        inventory.setItem(6, recipeRegistry.getMachineItem("SATELLITE_TERMINAL"));
        inventory.setItem(7, recipeRegistry.getMachineItem("ORBITAL_REMOTE"));
        
        // Эпоха атома
        inventory.setItem(9, recipeRegistry.getMachineItem("COOLANT_TANK"));
        inventory.setItem(10, recipeRegistry.getMachineItem("REACTOR_CORE"));
        inventory.setItem(11, recipeRegistry.getMachineItem("CENTRIFUGE"));
        inventory.setItem(12, recipeRegistry.getMachineItem("MASS_FABRICATOR"));
        inventory.setItem(13, recipeRegistry.getMachineItem("ENERGY_STORAGE"));
        
        inventory.setItem(18, recipeRegistry.getUraniumItems().getItem("URANIUM_RAW"));
        inventory.setItem(19, recipeRegistry.getUraniumItems().getItem("URANIUM_INGOT"));
        inventory.setItem(20, recipeRegistry.getUraniumItems().getItem("URANIUM_ENRICHED"));
        inventory.setItem(21, recipeRegistry.getUraniumItems().getItem("HAZMAT_HELMET"));
        inventory.setItem(22, recipeRegistry.getUraniumItems().getItem("HAZMAT_CHESTPLATE"));
        inventory.setItem(23, recipeRegistry.getUraniumItems().getItem("HAZMAT_LEGGINGS"));
        inventory.setItem(24, recipeRegistry.getUraniumItems().getItem("HAZMAT_BOOTS"));
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }
}
