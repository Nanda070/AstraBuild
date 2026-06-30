package com.astrabuild.machine;

import com.astrabuild.storage.MachineDataStorage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;

public class CoolantTankWorker {

    private final Location tankLocation;

    public static final int MAX_COOLANT = 100000; // 100 ведер
    private int coolantMb = 0;

    public CoolantTankWorker(Location tankLocation, MachineDataStorage storage) {
        this.tankLocation = tankLocation;
        // Загрузить из NBT, если мы хотим сохранять между рестартами. 
        // Для простоты пока храним в переменной, при пересоздании чанка она сбросится,
        // поэтому лучше хранить в NBT блока.
    }

    private int tickCounter = 0;

    public void runTick() {
        if (coolantMb < MAX_COOLANT) {
            tickCounter++;
            if (tickCounter >= 20) {
                tickCounter = 0;
                absorbWaterFromNeighbors();
            }
        }
    }

    private void absorbWaterFromNeighbors() {
        int absorbed = 0;
        Block center = tankLocation.getBlock();

        // Проверяем 6 сторон
        int[][] directions = {{1,0,0}, {-1,0,0}, {0,1,0}, {0,-1,0}, {0,0,1}, {0,0,-1}};
        for (int[] dir : directions) {
            Block neighbor = center.getRelative(dir[0], dir[1], dir[2]);
            Material type = neighbor.getType();
            
            if (type == Material.WATER) {
                // Если это источник воды (уровень 0)
                if (neighbor.getBlockData() instanceof Levelled levelled) {
                    if (levelled.getLevel() == 0) {
                        absorbed += 200; // Вытягиваем по 200 mB в секунду (батчинг)
                        // Мы не уничтожаем блок воды, имитируя помпу из бесконечного источника
                    }
                }
            } else if (type == Material.WATER_CAULDRON) {
                // Вытягиваем из котла
                if (neighbor.getBlockData() instanceof Levelled levelled) {
                    int level = levelled.getLevel();
                    if (level > 0) {
                        absorbed += 333; // Треть ведра
                        levelled.setLevel(level - 1);
                        neighbor.setBlockData(levelled);
                        if (level - 1 == 0) {
                            neighbor.setType(Material.CAULDRON);
                        }
                    }
                }
            }
            
            if (coolantMb + absorbed >= MAX_COOLANT) {
                break;
            }
        }
        
        addCoolant(absorbed);
    }

    public void addCoolant(int amount) {
        this.coolantMb += amount;
        if (this.coolantMb > MAX_COOLANT) {
            this.coolantMb = MAX_COOLANT;
        }
    }

    public int consumeCoolant(int amount) {
        if (this.coolantMb >= amount) {
            this.coolantMb -= amount;
            return amount;
        } else {
            int consumed = this.coolantMb;
            this.coolantMb = 0;
            return consumed;
        }
    }

    public int getCoolantMb() {
        return coolantMb;
    }

    public Location getLocation() {
        return tankLocation;
    }
}
