package com.astrabuild.item;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class RecipeRegistry {

    private final Plugin plugin;
    private final NamespacedKey machineItemKey;

    public RecipeRegistry(Plugin plugin) {
        this.plugin = plugin;
        this.machineItemKey = new NamespacedKey(plugin, "machine_item_type");
    }

    public void registerAll() {
        registerQuarry();
        registerPipe();
        registerConnector();
    }

    public ItemStack getMachineItem(String type) {
        return switch (type) {
            case "QUARRY" -> createQuarryItem();
            case "PIPE" -> createPipeItem();
            case "CONNECTOR_INSERT" -> createConnectorItem();
            default -> null;
        };
    }

    private ItemStack createQuarryItem() {
        ItemStack item = new ItemStack(Material.BLAST_FURNACE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6Карьер [Tier 1]");
        meta.setLore(List.of("§7Автоматически добывает ресурсы", "§7в чанке. Требует топливо."));
        meta.getPersistentDataContainer().set(machineItemKey, PersistentDataType.STRING, "QUARRY");
        item.setItemMeta(meta);
        return item;
    }


    private ItemStack createPipeItem() {
        // Синяя стеклянная панель идеально симулирует трубу
        ItemStack item = new ItemStack(Material.CYAN_STAINED_GLASS_PANE);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§bЛогистическая труба");
        meta.getPersistentDataContainer().set(machineItemKey, org.bukkit.persistence.PersistentDataType.STRING, "PIPE");
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createConnectorItem() {
        ItemStack item = new ItemStack(Material.DROPPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§eКоннектор входа");
        meta.getPersistentDataContainer().set(machineItemKey, PersistentDataType.STRING, "CONNECTOR_INSERT");
        item.setItemMeta(meta);
        return item;
    }


    private void registerQuarry() {
        ItemStack quarry = new ItemStack(Material.BLAST_FURNACE);
        ItemMeta meta = quarry.getItemMeta();
        meta.setDisplayName("§6Карьер [Tier 1]");
        meta.setLore(List.of("§7Автоматически добывает ресурсы", "§7в чанке. Требует топливо."));
        meta.getPersistentDataContainer().set(machineItemKey, PersistentDataType.STRING, "QUARRY");
        quarry.setItemMeta(meta);

        NamespacedKey key = new NamespacedKey(plugin, "astrabuild_quarry_t1");
        ShapedRecipe recipe = new ShapedRecipe(key, quarry);
        recipe.shape("DDD", "OBO", "RER");
        recipe.setIngredient('D', Material.DIAMOND_PICKAXE);
        recipe.setIngredient('O', Material.OBSIDIAN);
        recipe.setIngredient('B', Material.BEACON);
        recipe.setIngredient('R', Material.REDSTONE_BLOCK);
        recipe.setIngredient('E', Material.EMERALD_BLOCK);

        Bukkit.addRecipe(recipe);
    }

    private void registerPipe() {
        ItemStack pipe = getMachineItem("PIPE");
        NamespacedKey key = new NamespacedKey(plugin, "astrabuild_pipe");
        ShapedRecipe recipe = new ShapedRecipe(key, pipe);
        recipe.shape("GGG", "RDR", "GGG");
        recipe.setIngredient('G', Material.GLASS);
        recipe.setIngredient('R', Material.REDSTONE);
        recipe.setIngredient('D', Material.DIAMOND);
        org.bukkit.Bukkit.addRecipe(recipe);
    }

    private void registerConnector() {
        ItemStack connector = new ItemStack(Material.DROPPER);
        ItemMeta meta = connector.getItemMeta();
        meta.setDisplayName("§eКоннектор входа");
        meta.getPersistentDataContainer().set(machineItemKey, PersistentDataType.STRING, "CONNECTOR_INSERT");
        connector.setItemMeta(meta);

        NamespacedKey key = new NamespacedKey(plugin, "astrabuild_connector");
        ShapedRecipe recipe = new ShapedRecipe(key, connector);
        recipe.shape(" I ", "IDI", " I ");
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('D', Material.DROPPER);

        Bukkit.addRecipe(recipe);
    }
    
    public String getMachineItemType(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer().get(machineItemKey, PersistentDataType.STRING);
    }
}