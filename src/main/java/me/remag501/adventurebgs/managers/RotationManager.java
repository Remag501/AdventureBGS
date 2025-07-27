package me.remag501.adventurebgs.managers;

import me.remag501.adventurebgs.AdventureBGS;
import org.bukkit.configuration.file.FileConfiguration;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class RotationManager {

    private final AdventureBGS plugin;
    private final List<String> worlds;
    private final int cycleMinutes;
    private final Instant startCycle;

    public RotationManager(AdventureBGS plugin) {
        this.plugin = plugin;
        FileConfiguration config = plugin.getConfig();

        this.cycleMinutes = config.getInt("rotation.cycle-minutes");
        this.worlds = config.getStringList("rotation.worlds");
        String startCycleStr = config.getString("rotation.start-cycle");
        this.startCycle = Instant.parse(startCycleStr);
    }

    public List<String> getWorlds() {
        return worlds;
    }

    public int getCurrentWorldIndex() {
        Instant now = Instant.now();
        long minutesSinceStart = Duration.between(startCycle, now).toMinutes();
        long cycleNumber = minutesSinceStart / cycleMinutes;
        return (int) (cycleNumber % worlds.size());
    }

    public String getCurrentWorldName() {
        return worlds.get(getCurrentWorldIndex());
    }
}

