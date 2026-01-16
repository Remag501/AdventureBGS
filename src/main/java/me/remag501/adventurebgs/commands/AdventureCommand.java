package me.remag501.adventurebgs.commands;

import me.remag501.adventurebgs.AdventureBGS;
import me.remag501.adventurebgs.managers.GuiManager;
import me.remag501.adventurebgs.managers.PDCManager;
import me.remag501.adventurebgs.managers.RotationManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Rotation;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class AdventureCommand implements CommandExecutor {

    private final AdventureBGS plugin;
    private final PDCManager pdcManager;
    private final GuiManager guiManager;
    private final RotationManager rotationManager;

    public AdventureCommand(AdventureBGS plugin, PDCManager pdcManager, RotationManager rotationManager, GuiManager guiManager) {
        this.plugin = plugin;
        this.pdcManager = pdcManager;
        this.guiManager = guiManager;
        this.rotationManager = rotationManager;
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

        // Handle /adventure tp <track> <player>
        if (args.length == 3 && args[0].equalsIgnoreCase("tp")) {
            if (!sender.hasPermission("adventure.admin")) {
                sender.sendMessage("You do not have permission to use this command.");
                return true;
            }

            String trackName = args[1];
            String playerName = args[2];

            // Handle teleport logic
            String currentWorld = rotationManager.getTrackById(trackName).getCurrentWorld().getId();
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "rtp player " + playerName + " " + currentWorld);
            pdcManager.syncPlayerToWorld(Bukkit.getPlayer(playerName), Bukkit.getWorld(currentWorld));

            sender.sendMessage("Attempting to teleport " + playerName + " to track: " + trackName);

            return true;
        }

        // Use GUI manager to handle adventure (Ensure sender is a player)
        if (sender instanceof Player) {
            guiManager.openAdventureGUI((Player) sender);
        } else {
            sender.sendMessage("Only players can open the Adventure GUI.");
        }

        return true;
    }

}