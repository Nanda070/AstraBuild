package com.astrabuild.strike;

import com.astrabuild.item.RecipeRegistry;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.List;

@SuppressWarnings("deprecation")
public class OrbitalStrikeListener implements Listener {

    private final OrbitalStrikeManager strikeManager;
    private final RecipeRegistry recipeRegistry;
    private final NamespacedKey modeKey;

    public OrbitalStrikeListener(Plugin plugin, OrbitalStrikeManager strikeManager, RecipeRegistry recipeRegistry) {
        this.strikeManager = strikeManager;
        this.recipeRegistry = recipeRegistry;
        this.modeKey = new NamespacedKey(plugin, "orbital_strike_mode");
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType().isAir()) return;

        String machineType = recipeRegistry.getMachineItemType(item);
        if (!"ORBITAL_REMOTE".equals(machineType)) return;

        event.setCancelled(true);

        Action action = event.getAction();
        ItemMeta meta = item.getItemMeta();
        String currentMode = meta.getPersistentDataContainer().getOrDefault(modeKey, PersistentDataType.STRING, "NUKE");

        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            // Переключение режимов
            String newMode = switch (currentMode) {
                case "NUKE" -> "MEDIUM";
                case "MEDIUM" -> "STAB";
                default -> "NUKE";
            };

            String modeDisplay = switch (newMode) {
                case "NUKE" -> "§cNuke (Радиус 30)";
                case "MEDIUM" -> "§6Medium (Радиус 5)";
                default -> "§bStab (Точечный)";
            };

            meta.getPersistentDataContainer().set(modeKey, PersistentDataType.STRING, newMode);
            meta.setLore(List.of(
                    "§7[ЛКМ] - Смена режима",
                    "§7[ПКМ] - Запуск протокола уничтожения",
                    "",
                    "§eРежим: " + modeDisplay
            ));
            item.setItemMeta(meta);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 2.0f);
            player.sendMessage("§aРежим удара изменен на " + modeDisplay);
        } else if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            // Запуск удара
            Block targetBlock = player.getTargetBlockExact(200);
            if (targetBlock == null) {
                player.sendMessage("§cЦель слишком далеко!");
                return;
            }

            // Удаляем предмет (одноразовый)
            item.setAmount(item.getAmount() - 1);

            player.sendMessage("§c[!] ПРОТОКОЛ УНИЧТОЖЕНИЯ ЗАПУЩЕН! УХОДИТЕ ИЗ ЗОНЫ ПОРАЖЕНИЯ!");
            strikeManager.initiateStrike(player, targetBlock, currentMode);
        }
    }
}
