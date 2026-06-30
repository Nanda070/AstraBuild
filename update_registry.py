import os

file_path = r'src\main\java\com\astrabuild\item\RecipeRegistry.java'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('registerPipe();', 'registerEnergyPipe();\n        registerItemPipe();\n        registerRadiationCleaner();')
content = content.replace('case "PIPE" -> createPipeItem();', 'case "ENERGY_PIPE" -> createEnergyPipeItem();\n            case "ITEM_PIPE" -> createItemPipeItem();\n            case "RADIATION_CLEANER" -> createRadiationCleanerItem();')

pipe_methods = '''    private ItemStack createEnergyPipeItem() {
        ItemStack item = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§eЭнергетическая труба");
        meta.setLore(List.of("§7Проводит энергию EU."));
        meta.getPersistentDataContainer().set(machineItemKey, org.bukkit.persistence.PersistentDataType.STRING, "ENERGY_PIPE");
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createItemPipeItem() {
        ItemStack item = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§bЛогистическая труба");
        meta.setLore(List.of("§7Перемещает предметы."));
        meta.getPersistentDataContainer().set(machineItemKey, org.bukkit.persistence.PersistentDataType.STRING, "ITEM_PIPE");
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createRadiationCleanerItem() {
        ItemStack item = new ItemStack(Material.BEACON);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§dОчиститель Радиации");
        meta.setLore(List.of("§7Очищает радиацию в радиусе 100 блоков.", "§cОдноразовый!"));
        meta.getPersistentDataContainer().set(machineItemKey, org.bukkit.persistence.PersistentDataType.STRING, "RADIATION_CLEANER");
        item.setItemMeta(meta);
        return item;
    }'''

content = content.replace('    private ItemStack createPipeItem() {', pipe_methods + '\n\n    private ItemStack createPipeItem_old() {')

register_methods = '''    private void registerEnergyPipe() {
        ItemStack pipe = getMachineItem("ENERGY_PIPE");
        NamespacedKey key = new NamespacedKey(plugin, "astrabuild_energy_pipe");
        ShapedRecipe recipe = new ShapedRecipe(key, pipe);
        recipe.shape("GGG", "RDR", "GGG");
        recipe.setIngredient('G', Material.GLASS);
        recipe.setIngredient('R', Material.REDSTONE);
        recipe.setIngredient('D', Material.GOLD_INGOT);
        Bukkit.addRecipe(recipe);
    }
    
    private void registerItemPipe() {
        ItemStack pipe = getMachineItem("ITEM_PIPE");
        NamespacedKey key = new NamespacedKey(plugin, "astrabuild_item_pipe");
        ShapedRecipe recipe = new ShapedRecipe(key, pipe);
        recipe.shape("GGG", "RDR", "GGG");
        recipe.setIngredient('G', Material.GLASS);
        recipe.setIngredient('R', Material.REDSTONE);
        recipe.setIngredient('D', Material.LAPIS_LAZULI);
        Bukkit.addRecipe(recipe);
    }
    
    private void registerRadiationCleaner() {
        ItemStack item = getMachineItem("RADIATION_CLEANER");
        NamespacedKey key = new NamespacedKey(plugin, "astrabuild_radiation_cleaner");
        ShapedRecipe recipe = new ShapedRecipe(key, item);
        recipe.shape(" R ", "IDI", " R ");
        recipe.setIngredient('R', Material.REDSTONE);
        recipe.setIngredient('D', Material.DIAMOND_BLOCK);
        recipe.setIngredient('I', Material.IRON_BLOCK);
        Bukkit.addRecipe(recipe);
    }'''

content = content.replace('    private void registerPipe() {', register_methods + '\n\n    private void registerPipe_old() {')

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Updated RecipeRegistry.java")