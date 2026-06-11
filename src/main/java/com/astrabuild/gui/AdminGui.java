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
        this.inventory = Bukkit.createInventory(this, 27, "§cАдмин-панель AstraBuild");
        
        // Список всех предметов для выдачи
        inventory.setItem(0, recipeRegistry.getMachineItem("QUARRY"));
        inventory.setItem(1, recipeRegistry.getMachineItem("PIPE"));
        inventory.setItem(2, recipeRegistry.getMachineItem("CONNECTOR_INSERT"));
        inventory.setItem(3, recipeRegistry.getMachineItem("UPGRADE_FORTUNE"));
        inventory.setItem(4, recipeRegistry.getMachineItem("UPGRADE_SILK_TOUCH"));
        inventory.setItem(5, recipeRegistry.getMachineItem("UPGRADE_OVERCLOCKER"));
        inventory.setItem(6, recipeRegistry.getMachineItem("SATELLITE_TERMINAL"));
        inventory.setItem(7, recipeRegistry.getMachineItem("ORBITAL_REMOTE"));
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }
}
