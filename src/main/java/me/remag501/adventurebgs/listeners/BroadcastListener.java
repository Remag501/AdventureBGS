package me.remag501.adventurebgs.listeners;

import me.remag501.adventurebgs.AdventureBGS;
import me.remag501.adventurebgs.AdventureSettings;
import me.remag501.adventurebgs.SettingsProvider;
import me.remag501.adventurebgs.managers.RotationManager;
import me.remag501.adventurebgs.tasks.BroadcastTask;
import org.bukkit.boss.BossBar;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.plugin.Plugin;

public class BroadcastListener implements Listener {

    private final BroadcastTask task;
    private final RotationManager rotationManager;

    public BroadcastListener(RotationManager rotationManager, BroadcastTask task) {
        this.rotationManager = rotationManager;
        this.task = task;
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {

        BossBar bar = task.getWarningBossBar();
        if (bar == null) return;

        String activeWorld = rotationManager.getCurrentWorld().getId();

        if (event.getPlayer().getWorld().getName().equals(activeWorld)) {
            bar.addPlayer(event.getPlayer());
        } else {
            bar.removePlayer(event.getPlayer());
        }
    }


}
