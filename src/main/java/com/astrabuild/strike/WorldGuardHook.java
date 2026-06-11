package com.astrabuild.strike;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class WorldGuardHook {

    public static boolean canBreak(Location location, Player player) {
        try {
            com.sk89q.worldedit.util.Location weLoc = BukkitAdapter.adapt(location);
            LocalPlayer localPlayer = player != null ? WorldGuardPlugin.inst().wrapPlayer(player) : null;
            
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();
            
            // Проверяем, разрешено ли этому игроку строить/ломать в данной точке.
            // Если тут чужой приват — вернет false. Если свой приват или пустой мир — true.
            return query.testBuild(weLoc, localPlayer);
        } catch (Throwable e) {
            // Если WorldGuard нет или произошла ошибка
            return true;
        }
    }
}
