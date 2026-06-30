package com.astrabuild.strike;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class OrbitalStrikeManager {

    private final Plugin plugin;

    public OrbitalStrikeManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public void initiateStrike(Player player, Block targetBlock, String mode) {
        Location targetLoc = targetBlock.getLocation();
        World world = targetLoc.getWorld();

        // Предупреждающая фаза (10 секунд)
        new BukkitRunnable() {
            int ticks = 0;
            final int MAX_TICKS = 200; // 10 секунд

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
                        world.spawnParticle(Particle.DUST, targetLoc.clone().add(0.5, y - targetLoc.getBlockY(), 0.5),
                                2, 0, 0, 0, new Particle.DustOptions(org.bukkit.Color.RED, 3.0f));
                    }
                }

                // Звук тревоги
                if (ticks % 10 == 0) {
                    world.playSound(targetLoc, Sound.BLOCK_BEACON_ACTIVATE, 5.0f, 2.0f);
                    if (ticks == 0)
                        world.playSound(targetLoc, Sound.ENTITY_ENDER_DRAGON_GROWL, 5.0f, 0.5f);
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void executeStrike(Player player, Location center, String mode) {
        World world = center.getWorld();
        if (world == null)
            return;

        world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 10.0f, 0.5f);

        int maxRadius = switch (mode) {
            case "REACTOR_MELTDOWN" -> 50;
            case "NUKE" -> 30;
            case "MEDIUM" -> 5;
            default -> 0; // STAB
        };

        double damage = switch (mode) {
            case "REACTOR_MELTDOWN" -> 1000.0;
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
            
            // Если это ядерка (не взрыв реактора), планируем регенерацию ландшафта через 24 часа (20 тиков * 60 сек * 60 мин * 24 часа = 1728000 тиков)
            if ("NUKE".equals(mode)) {
                new TerrainRegenTask(plugin).scheduleNukeRegen(center, maxRadius + 10, 1728000L);
                
                // Загрязняем чанки радиацией
                com.astrabuild.strike.RadiationManager rm = com.astrabuild.AstraBuildPlugin.getInstance().getRadiationManager();
                for (int x = -maxRadius / 16 - 1; x <= maxRadius / 16 + 1; x++) {
                    for (int z = -maxRadius / 16 - 1; z <= maxRadius / 16 + 1; z++) {
                        rm.contaminateChunk(center.clone().add(x * 16, 0, z * 16).getChunk(), 86400000L); // 24 часа
                    }
                }
            }
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
