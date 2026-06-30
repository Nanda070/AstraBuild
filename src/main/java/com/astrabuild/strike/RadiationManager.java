package com.astrabuild.strike;

import com.astrabuild.item.RecipeRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class RadiationManager {

    private final Plugin plugin;
    private final RecipeRegistry recipeRegistry;
    private final NamespacedKey radiationKey;

    public RadiationManager(Plugin plugin, RecipeRegistry recipeRegistry) {
        this.plugin = plugin;
        this.recipeRegistry = recipeRegistry;
        this.radiationKey = new NamespacedKey(plugin, "chunk_radiation_end_time");
        startRadiationTask();
    }

        public void cleanArea(Location center, int radius) {
        int chunkRadius = (radius / 16) + 1;
        int centerCX = center.getBlockX() >> 4;
        int centerCZ = center.getBlockZ() >> 4;
        org.bukkit.World world = center.getWorld();
        
        for (int x = -chunkRadius; x <= chunkRadius; x++) {
            for (int z = -chunkRadius; z <= chunkRadius; z++) {
                Chunk chunk = world.getChunkAt(centerCX + x, centerCZ + z);
                chunk.getPersistentDataContainer().remove(radiationKey);
            }
        }
    }
    public void contaminateChunk(Chunk chunk, long durationMillis) {
        long endTime = System.currentTimeMillis() + durationMillis;
        chunk.getPersistentDataContainer().set(radiationKey, PersistentDataType.LONG, endTime);
    }

    public boolean isChunkContaminated(Chunk chunk) {
        Long endTime = chunk.getPersistentDataContainer().get(radiationKey, PersistentDataType.LONG);
        if (endTime != null) {
            if (System.currentTimeMillis() < endTime) {
                return true;
            } else {
                chunk.getPersistentDataContainer().remove(radiationKey); // Очистка
                return false;
            }
        }
        return false;
    }

    private void startRadiationTask() {
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    checkPlayerRadiation(player);
                }
                
                ticks++;
                if (ticks >= 10) { // Раз в 10 секунд
                    ticks = 0;
                    // Проверка выкинутых радиоактивных предметов
                    for (org.bukkit.World world : Bukkit.getWorlds()) {
                        for (Entity entity : world.getEntitiesByClass(Item.class)) {
                            Item item = (Item) entity;
                            ItemStack stack = item.getItemStack();
                            String type = recipeRegistry.getMachineItemType(stack);
                            if ("DEPLETED_FUEL_ROD".equals(type) || "TOXIC_WASTE".equals(type)) {
                                // Заражаем чанк на 1 час реального времени, если предмет лежит на земле
                                contaminateChunk(item.getLocation().getChunk(), 3600000L);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // Раз в секунду
    }

    private void checkPlayerRadiation(Player player) {
        Location loc = player.getLocation();
        if (isChunkContaminated(loc.getChunk())) {
            if (!hasFullHazmat(player)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 60, 2));
                player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, 0));
            }
        }
    }

    private boolean hasFullHazmat(Player player) {
        ItemStack helmet = player.getInventory().getHelmet();
        ItemStack chest = player.getInventory().getChestplate();
        ItemStack legs = player.getInventory().getLeggings();
        ItemStack boots = player.getInventory().getBoots();

        return isHazmat(helmet, "HAZMAT_HELMET") &&
               isHazmat(chest, "HAZMAT_CHESTPLATE") &&
               isHazmat(legs, "HAZMAT_LEGGINGS") &&
               isHazmat(boots, "HAZMAT_BOOTS");
    }

    private boolean isHazmat(ItemStack item, String expectedType) {
        return expectedType.equals(recipeRegistry.getMachineItemType(item));
    }
}
