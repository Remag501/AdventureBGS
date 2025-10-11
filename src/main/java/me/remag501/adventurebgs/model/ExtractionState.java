package me.remag501.adventurebgs.model;

import org.bukkit.boss.BossBar;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * NOTE: This class is now redundant as its functionality has been moved
 * into the ExtractionZone class to support zone-centric, multi-player extraction.
 */
public class ExtractionState {
    private BukkitRunnable task;
    private BossBar bossBar;

    public ExtractionState(BukkitRunnable task, BossBar bossBar) {
        this.task = task;
        this.bossBar = bossBar;
    }

    public void cancel() {
        if (task != null) task.cancel();
        if (bossBar != null) bossBar.removeAll();
    }
}
