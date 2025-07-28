package me.remag501.adventurebgs.managers;

import me.remag501.adventurebgs.AdventureBGS;
import me.remag501.adventurebgs.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class TimeManager {

    private AdventureBGS plugin;

    public TimeManager(AdventureBGS plugin) {
        this.plugin = plugin;
    }

    public void startBroadcastTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            RotationManager rotation = plugin.getRotationManager();

            long minutesLeft = rotation.getMinutesUntilNextCycle();
            String currentMap = rotation.getCurrentWorld().getName();
            String nextMap = rotation.getNextWorld().getName();

            // Warning
            long warnMinutes = plugin.getConfig().getLong("broadcast.warn-minutes");
            if (minutesLeft == warnMinutes) {
                String msg = plugin.getConfig().getString("broadcast.warn-message");
                Bukkit.broadcastMessage(MessageUtil.format(msg, currentMap, nextMap, minutesLeft));
            }

            // New map event (detect cycle boundary)
            if (rotation.isNewCycle()) {
                String msg = plugin.getConfig().getString("broadcast.new-map-message");
                Bukkit.broadcastMessage(MessageUtil.format(msg, currentMap, nextMap, minutesLeft));
                // Apply penalty
                applyPenalty(rotation.getCurrentWorld().getName());
            }

        }, 20L, 20L); // Run every second
    }

    private void applyPenalty(String closedWorldName) {
        String penaltyMsg = plugin.getConfig().getString("penalty.message");
        String soundName = plugin.getConfig().getString("penalty.sound");

        String effectType = plugin.getConfig().getString("penalty.effect.type");
        int amplifier = plugin.getConfig().getInt("penalty.effect.amplifier", 1);

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

            // Apply Wither
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.getByName(effectType),
                    Integer.MAX_VALUE, // Permanent
                    amplifier,
                    false,
                    false,
                    true
            ));
        }
    }
}
