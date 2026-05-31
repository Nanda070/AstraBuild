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

@SuppressWarnings("deprecation")
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
        registerUpgradeFortune();
        registerUpgradeSilkTouch();
        registerUpgradeOverclocker();
    }

    public ItemStack getMachineItem(String type) {
        return switch (type) {
            case "QUARRY" -> createQuarryItem();
            case "PIPE" -> createPipeItem();
            case "CONNECTOR_INSERT" -> createConnectorItem();
            case "UPGRADE_FORTUNE" -> createUpgradeFortune();
            case "UPGRADE_SILK_TOUCH" -> createUpgradeSilkTouch();
            case "UPGRADE_OVERCLOCKER" -> createUpgradeOverclocker();
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

    private ItemStack createUpgradeFortune() {
        ItemStack item = new ItemStack(Material.LAPIS_LAZULI);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§bЧип Удачи II");
        meta.setLore(List.of("§7Увеличивает шанс двойной", "§7или тройной добычи руд."));
        meta.getPersistentDataContainer().set(machineItemKey, PersistentDataType.STRING, "UPGRADE_FORTUNE");
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createUpgradeSilkTouch() {
        ItemStack item = new ItemStack(Material.GOLD_INGOT);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§eЧип Шелкового касания");
        meta.setLore(List.of("§7Добывает блоки в их", "§7исходном виде."));
        meta.getPersistentDataContainer().set(machineItemKey, PersistentDataType.STRING, "UPGRADE_SILK_TOUCH");
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createUpgradeOverclocker() {
        ItemStack item = new ItemStack(Material.REDSTONE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§cЧип Оверклокера");
        meta.setLore(List.of("§7Ускоряет работу карьера,", "§7но увеличивает расход энергии."));
        meta.getPersistentDataContainer().set(machineItemKey, PersistentDataType.STRING, "UPGRADE_OVERCLOCKER");
        item.setItemMeta(meta);
        return item;
    }

    private void registerQuarry() {
        ItemStack quarry = getMachineItem("QUARRY");
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
        Bukkit.addRecipe(recipe);
    }

    private void registerConnector() {
        ItemStack connector = getMachineItem("CONNECTOR_INSERT");
        NamespacedKey key = new NamespacedKey(plugin, "astrabuild_connector");
        ShapedRecipe recipe = new ShapedRecipe(key, connector);
        recipe.shape(" I ", "IDI", " I ");
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('D', Material.DROPPER);
        Bukkit.addRecipe(recipe);
    }

    private void registerUpgradeFortune() {
        ItemStack item = getMachineItem("UPGRADE_FORTUNE");
        NamespacedKey key = new NamespacedKey(plugin, "astrabuild_upgrade_fortune");
        ShapedRecipe recipe = new ShapedRecipe(key, item);
        recipe.shape("LLL", "LAL", "LLL");
        recipe.setIngredient('L', Material.LAPIS_LAZULI);
        recipe.setIngredient('A', Material.DIAMOND);
        Bukkit.addRecipe(recipe);
    }

    private void registerUpgradeSilkTouch() {
        ItemStack item = getMachineItem("UPGRADE_SILK_TOUCH");
        NamespacedKey key = new NamespacedKey(plugin, "astrabuild_upgrade_silk_touch");
        ShapedRecipe recipe = new ShapedRecipe(key, item);
        recipe.shape("GGG", "GAG", "GGG");
        recipe.setIngredient('G', Material.GOLD_INGOT);
        recipe.setIngredient('A', Material.AMETHYST_SHARD);
        Bukkit.addRecipe(recipe);
    }

    private void registerUpgradeOverclocker() {
        ItemStack item = getMachineItem("UPGRADE_OVERCLOCKER");
        NamespacedKey key = new NamespacedKey(plugin, "astrabuild_upgrade_overclocker");
        ShapedRecipe recipe = new ShapedRecipe(key, item);
        recipe.shape("RRR", "ROR", "RRR");
        recipe.setIngredient('R', Material.REDSTONE);
        recipe.setIngredient('O', Material.OBSERVER);
        Bukkit.addRecipe(recipe);
    }
    
    public String getMachineItemType(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer().get(machineItemKey, PersistentDataType.STRING);
    }
}