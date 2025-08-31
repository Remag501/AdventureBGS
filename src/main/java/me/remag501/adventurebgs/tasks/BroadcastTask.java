package me.remag501.adventurebgs.tasks;

import me.remag501.adventurebgs.AdventureBGS;
import me.remag501.adventurebgs.managers.RotationManager;
import me.remag501.adventurebgs.util.MessageUtil;
import org.bukkit.Bukkit;

public class BroadcastTask implements Runnable {

    private AdventureBGS plugin;
    private boolean hasBroadcasted;
    public BroadcastTask (AdventureBGS plugin) {
        this.plugin = plugin;
        hasBroadcasted = false;
    }

    @Override
    public void run() {
            RotationManager rotation = plugin.getRotationManager();

            long minutesLeft = rotation.getMinutesUntilNextCycle();
            String currentMap = rotation.getCurrentWorld().getChatName();
            String nextMap = rotation.getNextWorld().getChatName();

            // Warning
            long warnMinutes = plugin.getConfig().getLong("broadcast.warn-minutes");
            if (minutesLeft == warnMinutes && !hasBroadcasted) {
                String msg = plugin.getConfig().getString("broadcast.warn-message");
                Bukkit.broadcastMessage(MessageUtil.format(msg, currentMap, nextMap, minutesLeft));
                hasBroadcasted = true;
            }

            // New map event (detect cycle boundary)
            if (rotation.isNewCycle()) {
                String msg = plugin.getConfig().getString("broadcast.new-map-message");
                Bukkit.broadcastMessage(MessageUtil.format(msg, currentMap, nextMap, minutesLeft));
                // Apply penalty
                plugin.getPenaltyManager().applyPenalty(rotation.getCurrentWorld().getId());
                hasBroadcasted = false; // Allow broadcast for next map
            }
    }

}
