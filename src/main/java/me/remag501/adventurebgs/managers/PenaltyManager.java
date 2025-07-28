package me.remag501.adventurebgs.managers;

import me.remag501.adventurebgs.AdventureBGS;
import me.remag501.adventurebgs.tasks.BroadcastTask;
import me.remag501.adventurebgs.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class PenaltyManager {

    private AdventureBGS plugin;
    private BroadcastTask broadcastTask;

    public PenaltyManager(AdventureBGS plugin) {
        this.plugin = plugin;
        this.broadcastTask = new BroadcastTask(plugin);
    }

    public void applyPenalty(String closedWorldName) {
        String penaltyMsg = plugin.getConfig().getString("penalty.message");
        String soundName = plugin.getConfig().getString("penalty.sound");

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
    }

    public void startBroadcastTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, broadcastTask, 20L, 20L);
    }
}
