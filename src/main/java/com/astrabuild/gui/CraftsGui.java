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
        this.inventory = Bukkit.createInventory(this, 54, "§8Крафты AstraBuild");
        
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
        
        // Атомная эра
        inventory.setItem(28, recipeRegistry.getMachineItem("COOLANT_TANK"));
        inventory.setItem(29, recipeRegistry.getMachineItem("REACTOR_CORE"));
        inventory.setItem(30, recipeRegistry.getMachineItem("CENTRIFUGE"));
        inventory.setItem(31, recipeRegistry.getMachineItem("MASS_FABRICATOR"));
        inventory.setItem(32, recipeRegistry.getMachineItem("ENERGY_STORAGE"));
        
        inventory.setItem(37, recipeRegistry.getUraniumItems().getItem("URANIUM_RAW"));
        inventory.setItem(38, recipeRegistry.getUraniumItems().getItem("HAZMAT_HELMET"));
        inventory.setItem(39, recipeRegistry.getUraniumItems().getItem("HAZMAT_CHESTPLATE"));
        inventory.setItem(40, recipeRegistry.getUraniumItems().getItem("HAZMAT_LEGGINGS"));
        inventory.setItem(41, recipeRegistry.getUraniumItems().getItem("HAZMAT_BOOTS"));
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }
}
