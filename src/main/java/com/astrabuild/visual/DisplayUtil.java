package com.astrabuild.visual;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Bukkit;

import java.util.List;

public class DisplayUtil {

    public static void animateItemPath(Plugin plugin, ItemStack itemStack, List<Location> path, int ticksPerBlock, Runnable onComplete) {
        if (path == null || path.isEmpty()) {
            if (onComplete != null) onComplete.run();
            return;
        }

        Location start = path.get(0).clone().add(0.5, 0.5, 0.5); // Центрируем в блоке
        World world = start.getWorld();
        if (world == null) return;

        // Спавним фейковый предмет. Он не имеет коллизий и не подбирается.
        ItemDisplay display = world.spawn(start, ItemDisplay.class, ent -> {
            ent.setItemStack(itemStack);
            ent.setBillboard(Display.Billboard.CENTER); // Поворачивается к игроку
            ent.setTeleportDuration(ticksPerBlock);     // Клиентская интерполяция
            ent.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.GROUND);
        });

        // Асинхронно/синхронно толкаем сущность по узлам
        new BukkitRunnable() {
            int index = 1;

            @Override
            public void run() {
                if (index >= path.size()) {
                    display.remove(); // Уничтожаем сущность по прибытии
                    if (onComplete != null) Bukkit.getScheduler().runTask(plugin, onComplete);
                    this.cancel();
                    return;
                }
                
                Location next = path.get(index).clone().add(0.5, 0.5, 0.5);
                display.teleport(next); // Отправляем пакет перемещения, клиент рисует плавный полет
                index++;
            }
        }.runTaskTimer(plugin, ticksPerBlock, ticksPerBlock); // Шаг равен времени интерполяции
    }

    public static void highlightBounds(Plugin plugin, Location controllerLoc, Location p1, Location p2) {
        org.bukkit.World world = p1.getWorld();
        if (world == null) return;

        int minX = Math.min(p1.getBlockX(), p2.getBlockX());
        int maxX = Math.max(p1.getBlockX(), p2.getBlockX()) + 1;
        int minZ = Math.min(p1.getBlockZ(), p2.getBlockZ());
        int maxZ = Math.max(p1.getBlockZ(), p2.getBlockZ()) + 1;
        
        // Рисуем на уровне самого карьера
        double y = controllerLoc.getBlockY() + 0.1;

        org.bukkit.Particle.DustOptions dust = new org.bukkit.Particle.DustOptions(org.bukkit.Color.RED, 1.2F);

        new org.bukkit.scheduler.BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks++ > 15) { // 3 секунды
                    this.cancel();
                    return;
                }
                
                // Квадрат на земле
                for (double x = minX; x <= maxX; x += 0.5) {
                    world.spawnParticle(org.bukkit.Particle.DUST, x, y, minZ, 1, dust);
                    world.spawnParticle(org.bukkit.Particle.DUST, x, y, maxZ, 1, dust);
                }
                for (double z = minZ; z <= maxZ; z += 0.5) {
                    world.spawnParticle(org.bukkit.Particle.DUST, minX, y, z, 1, dust);
                    world.spawnParticle(org.bukkit.Particle.DUST, maxX, y, z, 1, dust);
                }
                
                // Вертикальные лучи по углам (на 10 блоков вверх и вниз)
                for (double dy = y - 10; dy <= y + 10; dy += 1) {
                    world.spawnParticle(org.bukkit.Particle.DUST, minX, dy, minZ, 1, dust);
                    world.spawnParticle(org.bukkit.Particle.DUST, maxX, dy, minZ, 1, dust);
                    world.spawnParticle(org.bukkit.Particle.DUST, minX, dy, maxZ, 1, dust);
                    world.spawnParticle(org.bukkit.Particle.DUST, maxX, dy, maxZ, 1, dust);
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 4L);
    }
}