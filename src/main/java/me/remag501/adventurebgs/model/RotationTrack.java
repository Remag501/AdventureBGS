package me.remag501.adventurebgs.model;

import org.bukkit.boss.BossBar;
import org.bukkit.scheduler.BukkitRunnable;

import javax.sound.midi.Track;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public final class RotationTrack {

    private final String id; // e.g. "rotation-1"
    private final List<WorldInfo> worlds;
    private final int cycleMinutes;
    private final Instant startCycle;
    private final TrackGuiConfig gui;

    // Cached state (updated once per second)
    private long secondsUntilNextCycle;
    private boolean newCycle;
    private boolean hasBroadcasted = false;

    private BossBar warningBossBar;


    public RotationTrack(String id,
                         List<WorldInfo> worlds,
                         int cycleMinutes,
                         Instant startCycle,
                         TrackGuiConfig gui) {
        this.id = id;
        this.worlds = worlds;
        this.cycleMinutes = cycleMinutes;
        this.startCycle = startCycle;
        this.gui = gui;
        recalculate();
    }

    public void tick() {
        recalculate();
    }

    private void recalculate() {
        Instant now = Instant.now();
        long secondsSinceStart = Duration.between(startCycle, now).getSeconds();
        long cycleSeconds = cycleMinutes * 60L;

        long secondsIntoCycle = secondsSinceStart % cycleSeconds;
        this.secondsUntilNextCycle = cycleSeconds - secondsIntoCycle;
        this.newCycle = secondsIntoCycle == 0;
    }

    public long getSecondsUntilNextCycle() {
        return secondsUntilNextCycle;
    }

    public long getMinutesUntilNextCycle() {
        return secondsUntilNextCycle / 60;
    }

    public boolean isNewCycle() {
        return newCycle;
    }

    public int getCurrentWorldIndex() {
        Instant now = Instant.now();
        long minutesSinceStart = Duration.between(startCycle, now).toMinutes();
        long cycleNumber = minutesSinceStart / cycleMinutes;
        return (int) (cycleNumber % worlds.size());
    }

    public WorldInfo getCurrentWorld() {
        return worlds.get(getCurrentWorldIndex());
    }

    public WorldInfo getNextWorld() {
        return worlds.get((getCurrentWorldIndex() + 1) % worlds.size());
    }

    public List<WorldInfo> getWorlds() {
        return worlds;
    }

    public boolean containsWorld(String worldId) {
        return worlds.stream().anyMatch(w -> w.getId().equals(worldId));
    }

    public String getId() {
        return id;
    }

    public TrackGuiConfig getGui() {
        return gui;
    }

    public void setNewCycle(boolean newCycle) {
        this.newCycle = newCycle;
    }

    public boolean isHasBroadcasted() {
        return hasBroadcasted;
    }

    public void setHasBroadcasted(boolean hasBroadcasted) {
        this.hasBroadcasted = hasBroadcasted;
    }

    public BossBar getWarningBossBar() {
        return warningBossBar;
    }

    public void setWarningBossBar(BossBar warningBossBar) {
        this.warningBossBar = warningBossBar;
    }


}

