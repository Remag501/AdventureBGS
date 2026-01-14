package me.remag501.adventurebgs.managers;

import me.remag501.adventurebgs.AdventureBGS;
import me.remag501.adventurebgs.AdventureSettings;
import me.remag501.adventurebgs.model.RotationTrack;
import me.remag501.adventurebgs.model.WorldInfo;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

//public class RotationManager {
//
////    private final List<String> worlds;
//    private List<WorldInfo> worlds;
//    private int cycleMinutes;
//    private Instant startCycle;
//
//    public RotationManager(AdventureSettings settings) {
//        this.cycleMinutes = settings.getCycleMinutes();
//        this.worlds = settings.getWorlds();
//        this.startCycle = settings.getStartCycle();
//    }
//
//    public void reloadSettings(AdventureSettings settings) {
//        this.cycleMinutes = settings.getCycleMinutes();
//        this.worlds = settings.getWorlds();
//        this.startCycle = settings.getStartCycle();
//    }
//
//
//    public List<String> getWorlds() {
//        List<String> rv = new ArrayList<>();
//        for (WorldInfo worldInfo: worlds) {
//            rv.add(worldInfo.getId());
//        }
//        return rv;
//    }
//
//    public int getCurrentWorldIndex() {
//        Instant now = Instant.now();
//        long minutesSinceStart = Duration.between(startCycle, now).toMinutes();
//        long cycleNumber = minutesSinceStart / cycleMinutes;
//        return (int) (cycleNumber % worlds.size());
//    }
//
//    public String getNextWorldName() {
//        return worlds.get((getCurrentWorldIndex() + 1) % worlds.size()).getId();
//    }
//
//    public long getMinutesUntilNextCycle() {
//        long secondsLeft = getSecondsUntilNextCycle();
//
//        return secondsLeft / 60; // floor to minutes
//    }
//
//    public long getSecondsUntilNextCycle() {
//        Instant now = Instant.now();
//        long secondsSinceStart = Duration.between(startCycle, now).getSeconds();
//        long cycleSeconds = cycleMinutes * 60L;
//
//        long secondsIntoCycle = secondsSinceStart % cycleSeconds;
//
//        return cycleSeconds - secondsIntoCycle; // floor to minutes
//    }
//
//
//    public boolean isNewCycle() {
//        Instant now = Instant.now();
//        long secondsSinceStart = Duration.between(startCycle, now).getSeconds();
//        long cycleSeconds = cycleMinutes * 60L;
//        return (secondsSinceStart % cycleSeconds) == 0; // exactly at boundary
//    }
//
//    public String getCurrentWorldName() {
//        return worlds.get(getCurrentWorldIndex()).getId();
//    }
//
//    public WorldInfo getCurrentWorld() {
//        return worlds.get(getCurrentWorldIndex());
//    }
//
//    public WorldInfo getNextWorld() {
//        return worlds.get((getCurrentWorldIndex() + 1) % worlds.size());
//    }
//}
//


public class RotationManager {

    private final Map<String, RotationTrack> tracks = new HashMap<>();
    private final Map<String, RotationTrack> worldToTrack = new HashMap<>();
    private final AdventureBGS plugin;

    public RotationManager(AdventureBGS plugin, AdventureSettings settings) {
        this.plugin = plugin;
        loadTracks(settings);
        startTicker();
    }

    public void reloadSettings(AdventureSettings settings) {
        tracks.clear();
        worldToTrack.clear();
        loadTracks(settings);
    }

    private void loadTracks(AdventureSettings settings) {
        // For now: one track using existing config

            for (RotationTrack track : settings.getTracks().values()) {
                tracks.put(track.getId(), track);

                for (WorldInfo world : track.getWorlds()) {
                    worldToTrack.put(world.getId(), track);
                }
            }

    }

    private void startTicker() {
        Bukkit.getScheduler().runTaskTimer(
                plugin,
                () -> tracks.values().forEach(RotationTrack::tick),
                0L,
                20L
        );
    }

    public RotationTrack getTrackByWorld(World world) {
        return worldToTrack.get(world.getName());
    }

    public Collection<RotationTrack> getTracks() {
        return tracks.values();
    }
}
