package me.remag501.adventurebgs.commands;

import me.remag501.adventurebgs.AdventureBGS;
import me.remag501.adventurebgs.managers.GuiManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class AdventureCommand implements CommandExecutor {

    private final AdventureBGS plugin;
    private final GuiManager guiManager;

    public AdventureCommand(AdventureBGS plugin, GuiManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Handle /adventure reload
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("adventure.admin")) {
                sender.sendMessage("You do not have permission to use this command.");
                return true;
            }

            plugin.reloadPluginConfig();
            sender.sendMessage("Adventure plugin configuration reloaded.");
            return true;
        }

        // Use GUI manager to handle adventure
        guiManager.openAdventureGUI((Player) sender);
        return true;
    }

}