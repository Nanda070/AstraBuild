package com.astrabuild.command;

import com.astrabuild.gui.AdminGui;
import com.astrabuild.gui.CraftsGui;
import com.astrabuild.item.RecipeRegistry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class AstraBuildCommand implements CommandExecutor, TabCompleter {

    private final RecipeRegistry recipeRegistry;

    public AstraBuildCommand(RecipeRegistry recipeRegistry) {
        this.recipeRegistry = recipeRegistry;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Команда только для игроков.");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("menu")) {
            new com.astrabuild.gui.GuideMenuGui(recipeRegistry).open(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("crafts") || args[0].equalsIgnoreCase("крафты")) {
            new CraftsGui(recipeRegistry).open(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("admin")) {
            if (!player.isOp() && !player.hasPermission("astrabuild.admin")) {
                player.sendMessage("§cУ вас нет прав для этой команды.");
                return true;
            }
            new AdminGui(recipeRegistry).open(player);
            return true;
        }

        player.sendMessage("§cНеизвестный аргумент. Используйте /astrabuild <menu|crafts|admin>");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            String input = args[0].toLowerCase();
            
            for (String option : List.of("menu", "crafts", "крафты", "admin")) {
                if (option.toLowerCase().startsWith(input)) {
                    if (option.equals("admin") && !sender.isOp() && !sender.hasPermission("astrabuild.admin")) {
                        continue;
                    }
                    completions.add(option);
                }
            }
            return completions;
        }
        return List.of();
    }
}
