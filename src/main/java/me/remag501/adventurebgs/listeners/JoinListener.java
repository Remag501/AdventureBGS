package me.remag501.adventurebgs.listeners;

import me.remag501.adventurebgs.AdventureBGS;
import me.remag501.adventurebgs.managers.DeathManager;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class JoinListener implements Listener {
    private final JavaPlugin plugin;
    private final DeathManager deathManager;

    public JoinListener(AdventureBGS plugin) {
        this.plugin = plugin;
        this.deathManager = plugin.getDeathManager();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();

        NamespacedKey key = new NamespacedKey(plugin, "map_version");

        // 1. Get the current version of the world
        int worldVersion = world.getPersistentDataContainer().getOrDefault(key, PersistentDataType.INTEGER, 0);

        // 2. Get the version the player last saw
        int playerVersion = player.getPersistentDataContainer().getOrDefault(key, PersistentDataType.INTEGER, 0);

        // 3. Compare
        if (playerVersion < worldVersion) {
            // They were here before the last regeneration
            player.setHealth(0);
            player.sendMessage("§cThe map regenerated while you were away!");

            // Update their version so they don't die again until the next reset
            player.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, worldVersion);
        }
    }
}