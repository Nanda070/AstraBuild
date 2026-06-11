package com.astrabuild.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;
import java.util.Objects;

@SuppressWarnings("deprecation")
public class RecipeViewGui implements InventoryHolder {
    private final Inventory inventory;

    public RecipeViewGui(ItemStack resultItem) {
        this.inventory = Bukkit.createInventory(this, 27, "§8Рецепт предмета");
        
        ItemStack bg = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta bgMeta = bg.getItemMeta();
        bgMeta.setDisplayName(" ");
        bg.setItemMeta(bgMeta);

        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, bg);
        }

        // Кнопка назад
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName("§cНазад");
        back.setItemMeta(backMeta);
        inventory.setItem(18, back);

        // Результат
        inventory.setItem(15, resultItem);

        // Ищем НАШИЙ кастомный рецепт (по displayName), а не ванильный
        String resultName = resultItem.hasItemMeta() && resultItem.getItemMeta().hasDisplayName()
                ? resultItem.getItemMeta().getDisplayName()
                : null;

        ShapedRecipe foundRecipe = null;
        java.util.Iterator<Recipe> it = Bukkit.recipeIterator();
        while (it.hasNext()) {
            Recipe r = it.next();
            if (r instanceof ShapedRecipe shaped) {
                ItemStack recipeResult = shaped.getResult();
                if (resultName != null && recipeResult.hasItemMeta() && recipeResult.getItemMeta().hasDisplayName()) {
                    if (Objects.equals(recipeResult.getItemMeta().getDisplayName(), resultName)) {
                        foundRecipe = shaped;
                        break;
                    }
                } else if (resultName == null && recipeResult.getType() == resultItem.getType() && !recipeResult.hasItemMeta()) {
                    foundRecipe = shaped;
                    break;
                }
            }
        }

        if (foundRecipe != null) {
            String[] shape = foundRecipe.getShape();
            Map<Character, ItemStack> ingredientMap = foundRecipe.getIngredientMap();
            
            int[] slots = {2, 3, 4, 11, 12, 13, 20, 21, 22};
            int slotIdx = 0;
            
            for (String row : shape) {
                for (int c = 0; c < row.length(); c++) {
                    char ch = row.charAt(c);
                    ItemStack ing = ingredientMap.get(ch);
                    if (ing != null && ing.getType() != Material.AIR) {
                        inventory.setItem(slots[slotIdx], ing);
                    } else {
                        inventory.setItem(slots[slotIdx], new ItemStack(Material.AIR));
                    }
                    slotIdx++;
                }
                // Если строка короче 3 символов, заполняем оставшиеся слоты воздухом
                for (int pad = row.length(); pad < 3; pad++) {
                    inventory.setItem(slots[slotIdx], new ItemStack(Material.AIR));
                    slotIdx++;
                }
            }
        } else {
            // Если рецепта нет (например, Пульт из Терминала)
            ItemStack error = new ItemStack(Material.BARRIER);
            ItemMeta errMeta = error.getItemMeta();
            errMeta.setDisplayName("§cКрафт отсутствует");
            errMeta.setLore(java.util.List.of("§7Этот предмет добывается", "§7другим путем."));
            error.setItemMeta(errMeta);
            inventory.setItem(12, error);
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }
}
