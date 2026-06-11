package com.astrabuild.gui;

import com.astrabuild.item.RecipeRegistry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

@SuppressWarnings("deprecation")
public class CraftsGui implements InventoryHolder {
    private final Inventory inventory;

    public CraftsGui(RecipeRegistry recipeRegistry) {
        this.inventory = Bukkit.createInventory(this, 27, "§8Крафты AstraBuild");
        
        // Список всех предметов
        inventory.setItem(10, recipeRegistry.getMachineItem("QUARRY"));
        inventory.setItem(11, recipeRegistry.getMachineItem("PIPE"));
        inventory.setItem(12, recipeRegistry.getMachineItem("CONNECTOR_INSERT"));
        
        inventory.setItem(14, recipeRegistry.getMachineItem("UPGRADE_FORTUNE"));
        inventory.setItem(15, recipeRegistry.getMachineItem("UPGRADE_SILK_TOUCH"));
        inventory.setItem(16, recipeRegistry.getMachineItem("UPGRADE_OVERCLOCKER"));
        
        inventory.setItem(22, recipeRegistry.getMachineItem("SATELLITE_TERMINAL"));
        // Пульт не имеет крафта (он выдается из терминала), но мы можем показать его
        inventory.setItem(23, recipeRegistry.getMachineItem("ORBITAL_REMOTE"));
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }
}
