package me.remag501.adventurebgs.managers;

import me.remag501.adventurebgs.AdventureBGS;
import me.remag501.adventurebgs.AdventureSettings;
import me.remag501.adventurebgs.model.WorldInfo;
import org.bukkit.configuration.file.FileConfiguration;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RotationManager {

//    private final List<String> worlds;
    private final List<WorldInfo> worlds;
    private final int cycleMinutes;
    private final Instant startCycle;

    public RotationManager(AdventureSettings settings) {
        this.cycleMinutes = settings.getCycleMinutes();
        this.worlds = settings.getWorlds();
        this.startCycle = settings.getStartCycle();
    }


    public List<String> getWorlds() {
        List<String> rv = new ArrayList<>();
        for (WorldInfo worldInfo: worlds) {
            rv.add(worldInfo.getId());
        }
        return rv;
    }

    public int getCurrentWorldIndex() {
        Instant now = Instant.now();
        long minutesSinceStart = Duration.between(startCycle, now).toMinutes();
        long cycleNumber = minutesSinceStart / cycleMinutes;
        return (int) (cycleNumber % worlds.size());
    }

    public String getNextWorldName() {
        return worlds.get((getCurrentWorldIndex() + 1) % worlds.size()).getId();
    }

    public long getMinutesUntilNextCycle() {
        long secondsLeft = getSecondsUntilNextCycle();

        return secondsLeft / 60; // floor to minutes
    }

    public long getSecondsUntilNextCycle() {
        Instant now = Instant.now();
        long secondsSinceStart = Duration.between(startCycle, now).getSeconds();
        long cycleSeconds = cycleMinutes * 60L;

        long secondsIntoCycle = secondsSinceStart % cycleSeconds;

        return cycleSeconds - secondsIntoCycle; // floor to minutes
    }


    public boolean isNewCycle() {
        Instant now = Instant.now();
        long secondsSinceStart = Duration.between(startCycle, now).getSeconds();
        long cycleSeconds = cycleMinutes * 60L;
        return (secondsSinceStart % cycleSeconds) == 0; // exactly at boundary
    }

    public String getCurrentWorldName() {
        return worlds.get(getCurrentWorldIndex()).getId();
    }

    public WorldInfo getCurrentWorld() {
        return worlds.get(getCurrentWorldIndex());
    }

    public WorldInfo getNextWorld() {
        return worlds.get((getCurrentWorldIndex() + 1) % worlds.size());
    }
}

