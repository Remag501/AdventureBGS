package me.remag501.adventurebgs.commands;

import me.remag501.adventurebgs.AdventureBGS;
import me.remag501.adventurebgs.managers.GuiManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AdventureCommand implements CommandExecutor {

    private final AdventureBGS plugin;
    private final GuiManager guiManager;

    public AdventureCommand(AdventureBGS plugin) {
        this.plugin = plugin;
        this.guiManager = new GuiManager(plugin);
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

        // Teleport player via BetterRTP
//        if (!(sender instanceof Player player)) {
//            sender.sendMessage("Only players can use this command.");
//            return true;
//        }
//
//        String currentWorldName = plugin.getRotationManager().getCurrentWorldName();
//
//        // Check if BetterRTP is installed
//        if (Bukkit.getPluginManager().getPlugin("BetterRTP") == null) {
//            player.sendMessage("BetterRTP is not installed on this server!");
//            return true;
//        }
//
//        // Execute RTP command (no spawn coords needed)
//        Bukkit.dispatchCommand(player, "rtp world " + currentWorldName);
        guiManager.openAdventureGUI((Player) sender);
        return true;
    }

}