package me.remag501.adventurebgs.managers;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class DeathManager {
    private final JavaPlugin plugin;
    private final File file;
    private final FileConfiguration config;
    private final Set<UUID> pendingDeaths;

    public DeathManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "pending_deaths.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        this.pendingDeaths = new HashSet<>();

        // Load existing UUIDs from file
        for (String s : config.getStringList("players")) {
            pendingDeaths.add(UUID.fromString(s));
        }
    }

    public void markPlayer(UUID uuid) {
        pendingDeaths.add(uuid);
        save();
    }

    public boolean isMarked(UUID uuid) {
        return pendingDeaths.contains(uuid);
    }

    public void unmarkPlayer(UUID uuid) {
        if (pendingDeaths.remove(uuid)) {
            save();
        }
    }

    private void save() {
        config.set("players", pendingDeaths.stream().map(UUID::toString).collect(Collectors.toList()));
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
