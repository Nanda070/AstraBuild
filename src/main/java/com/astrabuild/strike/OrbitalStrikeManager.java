package com.astrabuild.strike;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class OrbitalStrikeManager {

    private final Plugin plugin;

    public OrbitalStrikeManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public void initiateStrike(Player player, Block targetBlock, String mode) {
        Location targetLoc = targetBlock.getLocation();
        World world = targetLoc.getWorld();

        // Предупреждающая фаза (1.5 секунды)
        new BukkitRunnable() {
            int ticks = 0;
            final int MAX_TICKS = 30; // 1.5 секунды

            @Override
            public void run() {
                if (ticks >= MAX_TICKS) {
                    executeStrike(player, targetLoc, mode);
                    this.cancel();
                    return;
                }

                // Рисуем лазер реже (чтобы частицы не висели вечно)
                if (ticks % 5 == 0) {
                    for (int y = targetLoc.getBlockY(); y < 319; y += 4) {
                        world.spawnParticle(Particle.DUST, targetLoc.clone().add(0.5, y - targetLoc.getBlockY(), 0.5), 2, 0, 0, 0, new Particle.DustOptions(org.bukkit.Color.RED, 3.0f));
                    }
                }

                // Звук тревоги
                if (ticks % 10 == 0) {
                    world.playSound(targetLoc, Sound.BLOCK_BEACON_ACTIVATE, 5.0f, 2.0f);
                    if (ticks == 0) world.playSound(targetLoc, Sound.ENTITY_ENDER_DRAGON_GROWL, 5.0f, 0.5f);
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void executeStrike(Player player, Location center, String mode) {
        World world = center.getWorld();
        if (world == null) return;

        world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 10.0f, 0.5f);

        int maxRadius = switch (mode) {
            case "NUKE" -> 15;
            case "MEDIUM" -> 5;
            default -> 0; // STAB
        };

        double damage = switch (mode) {
            case "NUKE" -> 200.0;
            case "MEDIUM" -> 100.0;
            default -> 500.0;
        };

        // Урон сущностям
        double dmgRadius = Math.max(maxRadius, 3.0);
        for (Entity entity : world.getNearbyEntities(center, dmgRadius, dmgRadius, dmgRadius)) {
            if (entity instanceof LivingEntity living) {
                double distance = living.getLocation().distance(center);
                if (distance <= dmgRadius) {
                    living.damage(damage);
                }
            }
        }

        if (maxRadius > 0) {
            // Спавним реальные ТНТ кругами от центра
            spawnTNTCircle(world, center, maxRadius);
        }
    }

    private void spawnTNTCircle(World world, Location center, int maxRadius) {
        // Центральный усиленный ТНТ
        spawnTNT(world, center, 8.0f);
        
        // Кольца ТНТ
        for (int r = 4; r <= maxRadius; r += 4) {
            int tntCount = r * 2; // Чем больше радиус, тем больше ТНТ по кругу
            for (int i = 0; i < tntCount; i++) {
                double angle = 2 * Math.PI * i / tntCount;
                double x = center.getX() + r * Math.cos(angle);
                double z = center.getZ() + r * Math.sin(angle);
                
                // Спавним на уровне центра или чуть выше, чтобы разнести крыши
                Location tntLoc = new Location(world, x, center.getY() + 1, z);
                spawnTNT(world, tntLoc, 5.0f);
            }
        }
    }

    private void spawnTNT(World world, Location loc, float yield) {
        world.spawn(loc, org.bukkit.entity.TNTPrimed.class, tnt -> {
            tnt.setFuseTicks(0); // Взрывается моментально
            tnt.setYield(yield); // Радиус взрыва (ванильный = 4.0)
            tnt.setIsIncendiary(true); // Немного огня для красоты
        });
    }
}
