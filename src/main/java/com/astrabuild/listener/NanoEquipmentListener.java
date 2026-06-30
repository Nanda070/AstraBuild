package com.astrabuild.listener;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class NanoEquipmentListener implements Listener {

    private final Plugin plugin;
    private final NamespacedKey energyKey;
    private final NamespacedKey machineKey;

    public NanoEquipmentListener(Plugin plugin) {
        this.plugin = plugin;
        this.energyKey = new NamespacedKey(plugin, "astrabuild_energy");
        this.machineKey = new NamespacedKey(plugin, "machine_item_type");

        startFlightTask();
    }

    private void startFlightTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                ItemStack chestplate = player.getInventory().getChestplate();
                if (isNanoChestplate(chestplate)) {
                    int energy = getEnergy(chestplate);
                    if (energy >= 100) {
                        player.setAllowFlight(true);
                        if (player.isFlying()) {
                            setEnergy(chestplate, energy - 100, 10000);
                            // Обновляем предмет в инвентаре для лора
                            player.getInventory().setChestplate(chestplate);
                        }
                    } else {
                        player.setAllowFlight(false);
                        player.setFlying(false);
                    }
                } else if (player.getGameMode() == org.bukkit.GameMode.SURVIVAL || player.getGameMode() == org.bukkit.GameMode.ADVENTURE) {
                    player.setAllowFlight(false);
                    player.setFlying(false);
                }
            }
        }, 10L, 10L); // Раз в полсекунды
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Убираем полет если он багнулся
        Player player = event.getPlayer();
        if (!isNanoChestplate(player.getInventory().getChestplate()) && (player.getGameMode() == org.bukkit.GameMode.SURVIVAL || player.getGameMode() == org.bukkit.GameMode.ADVENTURE)) {
            player.setAllowFlight(false);
            player.setFlying(false);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            ItemStack weapon = player.getInventory().getItemInMainHand();
            if (isNanoSword(weapon)) {
                int energy = getEnergy(weapon);
                if (energy >= 500) {
                    event.setDamage(15.0); // Большой урон
                    setEnergy(weapon, energy - 500, 5000);
                    player.getInventory().setItemInMainHand(weapon);
                } else {
                    event.setDamage(1.0); // Разряжено
                }
            }
        }
    }

    private boolean isNanoChestplate(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        String type = item.getItemMeta().getPersistentDataContainer().get(machineKey, PersistentDataType.STRING);
        return "NANO_CHESTPLATE".equals(type);
    }

    private boolean isNanoSword(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        String type = item.getItemMeta().getPersistentDataContainer().get(machineKey, PersistentDataType.STRING);
        return "NANO_SWORD".equals(type);
    }

    private int getEnergy(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return 0;
        Integer energy = item.getItemMeta().getPersistentDataContainer().get(energyKey, PersistentDataType.INTEGER);
        return energy == null ? 0 : energy;
    }

    @SuppressWarnings("deprecation")
    private void setEnergy(ItemStack item, int energy, int maxEnergy) {
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(energyKey, PersistentDataType.INTEGER, energy);
        
        List<String> lore = meta.getLore();
        if (lore != null && lore.size() >= 4) {
            lore.set(3, "§eЗаряд: §a" + energy + " §7/ §a" + maxEnergy + " EU");
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
    }
}
