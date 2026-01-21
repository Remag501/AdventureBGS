package me.remag501.adventurebgs.listener;

import me.remag501.adventurebgs.manager.RotationManager;
import me.remag501.adventurebgs.model.RotationTrack;
import me.remag501.adventurebgs.task.BroadcastTask;
import org.bukkit.boss.BossBar;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class BroadcastListener implements Listener {

    private final BroadcastTask task;
    private final RotationManager rotationManager;

    public BroadcastListener(RotationManager rotationManager, BroadcastTask task) {
        this.rotationManager = rotationManager;
        this.task = task;
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {

//        Player player = event.getPlayer();
//        RotationTrack track = rotationManager.getTrackByWorld(player.getWorld());
//        if (track == null) return;
//        BossBar bar = track.getWarningBossBar();
//        if (bar == null) return;
//
//        String activeWorld = rotationManager.getTrackByWorld(player.getWorld()).getCurrentWorld().getId();
//
//        if (event.getPlayer().getWorld().getName().equals(activeWorld)) {
//            bar.addPlayer(event.getPlayer());
//        } else {
//            bar.removePlayer(event.getPlayer());
//        }

        for (RotationTrack track: rotationManager.getTracks()) {

            String activeWorld = track.getCurrentWorld().getId();
            BossBar bar = track.getWarningBossBar();
            if (bar == null) continue;

            if (event.getPlayer().getWorld().getName().equals(activeWorld)) {
                bar.addPlayer(event.getPlayer());
            }
            else
                bar.removePlayer(event.getPlayer());
        }

    }


}
