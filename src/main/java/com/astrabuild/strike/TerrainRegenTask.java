package com.astrabuild.strike;

import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class TerrainRegenTask {

    private final Plugin plugin;
    private CoreProtectAPI cpApi;

    public TerrainRegenTask(Plugin plugin) {
        this.plugin = plugin;
        Plugin cpPlugin = Bukkit.getServer().getPluginManager().getPlugin("CoreProtect");
        if (cpPlugin != null && cpPlugin.isEnabled() && cpPlugin instanceof CoreProtect) {
            CoreProtectAPI api = ((CoreProtect) cpPlugin).getAPI();
            if (api != null && api.APIVersion() >= 9) {
                this.cpApi = api;
                plugin.getLogger().info("CoreProtect API hooked for Terrain Regeneration.");
            }
        }
    }

    public void scheduleNukeRegen(Location center, int radius, long delayTicks) {
        if (cpApi == null) {
            plugin.getLogger().warning("CoreProtect is not available! Nuke regeneration cancelled.");
            return;
        }

        long timeStart = System.currentTimeMillis() / 1000L;

        new BukkitRunnable() {
            @Override
            public void run() {
                clearInventoriesInRadiusAndRollback(center, radius, timeStart);
            }
        }.runTaskLater(plugin, delayTicks);
    }

    private void clearInventoriesInRadiusAndRollback(Location center, int radius, long timeStart) {
        new BukkitRunnable() {
            int x = -radius;
            int y = -radius;
            int z = -radius;
            
            final int blocksPerTick = 50000; // Обрабатываем по 50k блоков за тик, чтобы не фризить

            @Override
            public void run() {
                int processed = 0;
                while (processed < blocksPerTick) {
                    if (x > radius) {
                        // Очистка завершена, запускаем откат
                        this.cancel();
                        runRollback(center, radius, timeStart);
                        return;
                    }
                    
                    Block block = center.clone().add(x, y, z).getBlock();
                    // WorldGuardHook: не очищаем сундуки внутри привата
                    if (WorldGuardHook.canBreak(block.getLocation(), null)) {
                        if (block.getState() instanceof Container container) {
                            container.getInventory().clear();
                        }
                    }
                    
                    processed++;
                    z++;
                    if (z > radius) {
                        z = -radius;
                        y++;
                        if (y > radius) {
                            y = -radius;
                            x++;
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void runRollback(Location center, int radius, long timeStart) {
        long timeEnd = System.currentTimeMillis() / 1000L;
        int time = (int) (timeEnd - timeStart + 10); // +10 секунд запаса

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                cpApi.performRollback(time, null, null, null, null, null, radius, center);
                plugin.getLogger().info("Nuke terrain regen completed via CoreProtect at " + center.toString());
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to perform CoreProtect rollback: " + e.getMessage());
            }
        });
    }
}
