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
        registerSatelliteTerminal();
        registerAtomicMachines();
        registerNanoArmor();
    }

    private void registerNanoArmor() {
        ItemStack chestplate = getMachineItem("NANO_CHESTPLATE");
        ShapedRecipe recipeChestplate = new ShapedRecipe(new NamespacedKey(plugin, "recipe_nano_chestplate"), chestplate);
        recipeChestplate.shape("S S", "IDI", "IRI");
        recipeChestplate.setIngredient('S', Material.NETHERITE_INGOT);
        recipeChestplate.setIngredient('I', Material.IRON_BLOCK);
        recipeChestplate.setIngredient('D', Material.DIAMOND_BLOCK);
        recipeChestplate.setIngredient('R', Material.REDSTONE_BLOCK);
        Bukkit.addRecipe(recipeChestplate);

        ItemStack sword = getMachineItem("NANO_SWORD");
        ShapedRecipe recipeSword = new ShapedRecipe(new NamespacedKey(plugin, "recipe_nano_sword"), sword);
        recipeSword.shape(" D ", " R ", " S ");
        recipeSword.setIngredient('D', Material.DIAMOND_BLOCK);
        recipeSword.setIngredient('R', Material.REDSTONE_BLOCK);
        recipeSword.setIngredient('S', Material.NETHERITE_SWORD);
        Bukkit.addRecipe(recipeSword);
    }

    public ItemStack getMachineItem(String type) {
        ItemStack item = switch (type) {
            case "QUARRY" -> createQuarryItem();
            case "PIPE" -> createPipeItem();
            case "CONNECTOR_INSERT" -> createConnectorItem();
            case "UPGRADE_FORTUNE" -> createUpgradeFortune();
            case "UPGRADE_SILK_TOUCH" -> createUpgradeSilkTouch();
            case "UPGRADE_OVERCLOCKER" -> createUpgradeOverclocker();
            case "SATELLITE_TERMINAL" -> createSatelliteTerminalItem();
            case "ORBITAL_REMOTE" -> createOrbitalRemoteItem();
            case "COOLANT_TANK" -> createSimpleMachineItem("COOLANT_TANK", Material.CAULDRON, "§bРезервуар Охладителя", "Хранит и поставляет воду реактору.");
            case "REACTOR_CORE" -> createSimpleMachineItem("REACTOR_CORE", Material.FURNACE, "§cКонтроллер Ядерного Реактора", "Сердце атомной станции.");
            case "CENTRIFUGE" -> createSimpleMachineItem("CENTRIFUGE", Material.DISPENSER, "§eЦентрифуга", "Обогащает уран.");
            case "MASS_FABRICATOR" -> createSimpleMachineItem("MASS_FABRICATOR", Material.DROPPER, "§dГенератор Материи", "Создает ценные ресурсы из мусора и энергии.");
            case "ENERGY_STORAGE" -> createSimpleMachineItem("ENERGY_STORAGE", Material.REDSTONE_BLOCK, "§aЭнергохранилище (МФСУ)", "Хранит до 10 000 000 EU.");
            case "NANO_CHESTPLATE" -> createNanoChestplate();
            case "NANO_SWORD" -> createNanoSword();
            default -> null;
        };

        if (item != null && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            int cmdId = UraniumItems.getCustomModelDataId(type);
            if (cmdId != -1) {
                meta.setCustomModelData(cmdId);
                item.setItemMeta(meta);
            }
        }
        return item;
    }

    private ItemStack createNanoChestplate() {
        ItemStack item = new ItemStack(Material.NETHERITE_CHESTPLATE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§bЭлектрический Джетпак");
        meta.setLore(List.of("§7Позволяет летать.", "§7Тратит энергию при полете.", "", "§eЗаряд: §a0 §7/ §a10000 EU"));
        meta.getPersistentDataContainer().set(machineItemKey, PersistentDataType.STRING, "NANO_CHESTPLATE");
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "astrabuild_energy"), PersistentDataType.INTEGER, 0);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createNanoSword() {
        ItemStack item = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§cНано-Сабля");
        meta.setLore(List.of("§7Огромный урон при наличии заряда.", "§7Бьет как палка без энергии.", "", "§eЗаряд: §a0 §7/ §a5000 EU"));
        meta.getPersistentDataContainer().set(machineItemKey, PersistentDataType.STRING, "NANO_SWORD");
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "astrabuild_energy"), PersistentDataType.INTEGER, 0);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createSimpleMachineItem(String id, Material mat, String name, String desc) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(List.of("§7" + desc));
        meta.getPersistentDataContainer().set(machineItemKey, PersistentDataType.STRING, id);
        item.setItemMeta(meta);
        return item;
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

    private ItemStack createSatelliteTerminalItem() {
        ItemStack item = new ItemStack(Material.BARREL);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§dСпутниковый Терминал");
        meta.setLore(List.of("§7Накапливает колоссальное количество", "§7энергии для вызова орбитального удара."));
        meta.getPersistentDataContainer().set(machineItemKey, PersistentDataType.STRING, "SATELLITE_TERMINAL");
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createOrbitalRemoteItem() {
        ItemStack item = new ItemStack(Material.GOLDEN_HOE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§cПульт Орбитального Удара");
        meta.setLore(List.of("§7[ЛКМ] - Смена режима", "§7[ПКМ] - Запуск протокола уничтожения", "", "§eРежим: §cNuke (Радиус 30)"));
        meta.getPersistentDataContainer().set(machineItemKey, PersistentDataType.STRING, "ORBITAL_REMOTE");
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

    private void registerSatelliteTerminal() {
        ItemStack terminal = getMachineItem("SATELLITE_TERMINAL");
        NamespacedKey key = new NamespacedKey(plugin, "astrabuild_satellite_terminal");
        ShapedRecipe recipe = new ShapedRecipe(key, terminal);
        recipe.shape("NEN", "DBD", "NON");
        recipe.setIngredient('N', Material.NETHERITE_BLOCK);
        recipe.setIngredient('E', Material.NETHER_STAR);
        recipe.setIngredient('D', Material.DIAMOND_BLOCK);
        recipe.setIngredient('B', Material.BEACON);
        recipe.setIngredient('O', Material.END_CRYSTAL);
        Bukkit.addRecipe(recipe);
    }

    private void registerAtomicMachines() {
        // Reactor Core
        ItemStack reactor = getMachineItem("REACTOR_CORE");
        NamespacedKey rKey = new NamespacedKey(plugin, "astrabuild_reactor_core");
        ShapedRecipe rRecipe = new ShapedRecipe(rKey, reactor);
        rRecipe.shape(" I ", "INI", " I ");
        rRecipe.setIngredient('I', Material.IRON_BLOCK);
        rRecipe.setIngredient('N', Material.NETHERITE_BLOCK);
        Bukkit.addRecipe(rRecipe);

        // Coolant Tank
        ItemStack tank = getMachineItem("COOLANT_TANK");
        NamespacedKey cKey = new NamespacedKey(plugin, "astrabuild_coolant_tank");
        ShapedRecipe cRecipe = new ShapedRecipe(cKey, tank);
        cRecipe.shape(" I ", "GBG", " I ");
        cRecipe.setIngredient('I', Material.IRON_BLOCK);
        cRecipe.setIngredient('G', Material.GLASS);
        cRecipe.setIngredient('B', Material.BUCKET);
        Bukkit.addRecipe(cRecipe);

        // Centrifuge
        ItemStack centri = getMachineItem("CENTRIFUGE");
        NamespacedKey cfKey = new NamespacedKey(plugin, "astrabuild_centrifuge");
        ShapedRecipe cfRecipe = new ShapedRecipe(cfKey, centri);
        cfRecipe.shape(" I ", "RDR", " I ");
        cfRecipe.setIngredient('I', Material.IRON_BLOCK);
        cfRecipe.setIngredient('R', Material.REDSTONE_BLOCK);
        cfRecipe.setIngredient('D', Material.DIAMOND);
        Bukkit.addRecipe(cfRecipe);

        // Mass Fabricator
        ItemStack massFab = getMachineItem("MASS_FABRICATOR");
        NamespacedKey mKey = new NamespacedKey(plugin, "astrabuild_mass_fabricator");
        ShapedRecipe mRecipe = new ShapedRecipe(mKey, massFab);
        mRecipe.shape("ODO", "DSD", "ODO");
        mRecipe.setIngredient('O', Material.OBSIDIAN);
        mRecipe.setIngredient('D', Material.DIAMOND_BLOCK);
        mRecipe.setIngredient('S', Material.NETHER_STAR);
        Bukkit.addRecipe(mRecipe);

        // Energy Storage
        ItemStack mfe = getMachineItem("ENERGY_STORAGE");
        NamespacedKey eKey = new NamespacedKey(plugin, "astrabuild_energy_storage");
        ShapedRecipe eRecipe = new ShapedRecipe(eKey, mfe);
        eRecipe.shape(" G ", "IRI", " G ");
        eRecipe.setIngredient('G', Material.GOLD_BLOCK);
        eRecipe.setIngredient('I', Material.IRON_BLOCK);
        eRecipe.setIngredient('R', Material.REDSTONE_BLOCK);
        Bukkit.addRecipe(eRecipe);
    }
    
    public String getMachineItemType(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer().get(machineItemKey, PersistentDataType.STRING);
    }

    private UraniumItems uraniumItems;

    public UraniumItems getUraniumItems() {
        return uraniumItems;
    }

    public void initUranium(Plugin plugin) {
        if (this.uraniumItems == null) {
            this.uraniumItems = new UraniumItems(plugin);
        }
    }

    public void registerUraniumRecipes() {
        // Крафт сырого урана (Custom Crafting for Uranium generation as requested)
        ItemStack rawUranium = uraniumItems.getItem("URANIUM_RAW");
        NamespacedKey rawKey = new NamespacedKey(plugin, "astrabuild_raw_uranium");
        ShapedRecipe rawRecipe = new ShapedRecipe(rawKey, rawUranium);
        rawRecipe.shape(" I ", "IDI", " I ");
        rawRecipe.setIngredient('I', Material.RAW_IRON);
        rawRecipe.setIngredient('D', Material.DEEPSLATE_COAL_ORE);
        Bukkit.addRecipe(rawRecipe);

        // Крафт брони (Hazmat Suit)
        registerHazmatPiece("HELMET", " L ", "LGL", "   ");
        registerHazmatPiece("CHESTPLATE", "L L", "LGL", "LLL");
        registerHazmatPiece("LEGGINGS", "LLL", "L L", "L L");
        registerHazmatPiece("BOOTS", "   ", "L L", "L L");
    }

    private void registerHazmatPiece(String piece, String row1, String row2, String row3) {
        ItemStack item = uraniumItems.getItem("HAZMAT_" + piece);
        NamespacedKey key = new NamespacedKey(plugin, "astrabuild_hazmat_" + piece.toLowerCase());
        ShapedRecipe recipe = new ShapedRecipe(key, item);
        recipe.shape(row1, row2, row3);
        recipe.setIngredient('L', Material.LEATHER);
        if (row1.contains("G") || row2.contains("G") || row3.contains("G")) {
            recipe.setIngredient('G', Material.GOLD_INGOT);
        }
        Bukkit.addRecipe(recipe);
    }
}