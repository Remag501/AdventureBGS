package me.remag501.adventurebgs.managers;

import me.remag501.adventurebgs.AdventureBGS;
import me.remag501.adventurebgs.AdventureSettings;
import me.remag501.adventurebgs.SettingsProvider;
import me.remag501.adventurebgs.tasks.BroadcastTask;
import me.remag501.adventurebgs.util.MessageUtil;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PenaltyManager {

    private final AdventureBGS plugin;
    private final BroadcastTask broadcastTask;
    private final RotationManager rotationManager;

    private AdventureSettings settings;


    public PenaltyManager(AdventureBGS plugin, RotationManager rotationManager, BroadcastTask broadcastTask, AdventureSettings settings) {
        this.plugin = plugin;
        this.broadcastTask = broadcastTask;
        this.settings = settings;
        this.rotationManager = rotationManager;

        // Lambda function to apply penalty based on broadcast task
        broadcastTask.setOnTimeUp(this::applyPenalty);
    }

    public void reloadSettings(AdventureSettings settings) {
        this.settings = settings;
    }

    public void applyPenalty(String closedWorldName) {
        String penaltyMsg = settings.getPenaltyMessage();
        String soundName = settings.getPenaltySound();

        plugin.getLogger().info("Withering the world: " + closedWorldName);

        // Wither away online players
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.getWorld().getName().equals(closedWorldName)) continue;

            // Send message
            player.sendMessage(MessageUtil.color(penaltyMsg));

            // Play sound
            try {
                Sound sound = Sound.valueOf(soundName);
                player.playSound(player.getLocation(), sound, 1f, 1f);
            } catch (IllegalArgumentException ignored) {
                plugin.getLogger().warning("Invalid sound in config: " + soundName);
            }

            penalizePlayer(player);

        }

        // Kill any players who logged out
        updateWorldVersion(Bukkit.getWorld(closedWorldName));

    }

    public void penalizePlayer(Player player) {
        // Apply Wither
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.WITHER,
                Integer.MAX_VALUE, // Permanent
                3,
                false,
                false,
                true
        ));
        // Apply Blindness
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.BLINDNESS,
                Integer.MAX_VALUE, // Permanent
                1,
                false,
                false,
                true
        ));
    }

    public void updateWorldVersion(World world) {
        NamespacedKey key = new NamespacedKey(plugin, "map_version");
        int currentVersion = world.getPersistentDataContainer().getOrDefault(key, PersistentDataType.INTEGER, 0);

        // Increment the version so old player "tickets" become invalid
        world.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, currentVersion + 1);
    }

    public void startBroadcastTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, broadcastTask, 20L, 20L);
    }
}
