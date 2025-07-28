package me.remag501.adventurebgs.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class MessageUtil {
    public static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String format(String message, String currentMap, String nextMap, long minutes) {
        Bukkit.getPluginManager().getPlugin("AdventureBGS").getLogger().info("hello");
        return color(message
                .replace("%current_map%", currentMap)
                .replace("%new_map%", nextMap)
                .replace("%minutes%", String.valueOf(minutes)));
    }
}
