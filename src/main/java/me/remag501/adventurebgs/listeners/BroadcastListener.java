package me.remag501.adventurebgs.listeners;

import me.remag501.adventurebgs.AdventureBGS;
import org.bukkit.boss.BossBar;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.plugin.Plugin;

public class BroadcastListener implements Listener {

    private AdventureBGS plugin;

    public BroadcastListener(AdventureBGS plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        BossBar bar = plugin.getBroadcastTask().getWarningBossBar();
        if (bar == null) return;

        String activeWorld = plugin.getRotationManager().getCurrentWorld().getId();

        if (event.getPlayer().getWorld().getName().equals(activeWorld)) {
            bar.addPlayer(event.getPlayer());
        } else {
            bar.removePlayer(event.getPlayer());
        }
    }


}
