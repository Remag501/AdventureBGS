package me.remag501.adventurebgs.tasks;

import me.remag501.adventurebgs.AdventureBGS;
import me.remag501.adventurebgs.managers.RotationManager;
import me.remag501.adventurebgs.util.MessageUtil;
import org.bukkit.Bukkit;

public class BroadcastTask implements Runnable {

    private AdventureBGS plugin;
    public BroadcastTask (AdventureBGS plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
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
                plugin.getPenaltyManager().applyPenalty(rotation.getCurrentWorld().getName());
            }
    }

}
