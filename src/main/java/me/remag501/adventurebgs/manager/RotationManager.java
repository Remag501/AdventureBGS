package me.remag501.adventurebgs.manager;

import me.remag501.adventurebgs.AdventureBGS;
import me.remag501.adventurebgs.setting.AdventureSettings;
import me.remag501.adventurebgs.model.RotationTrack;
import me.remag501.adventurebgs.model.WorldInfo;
import me.remag501.bgscore.api.task.TaskService;
import org.bukkit.Bukkit;
import org.bukkit.Rotation;
import org.bukkit.World;

import java.util.*;

public class RotationManager {

    private final TaskService taskService;
    private final Map<String, RotationTrack> tracks = new HashMap<>();
    private final Map<String, RotationTrack> worldToTrack = new HashMap<>();

    public RotationManager(TaskService taskService, AdventureSettings settings) {
        this.taskService = taskService;
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

        taskService.subscribe(AdventureBGS.SYSTEM_ID, 0, 20, (ticks) -> {
            tracks.values().forEach(RotationTrack::tick);
            return false;
        });

    }

    public RotationTrack getTrackByWorld(World world) {
        return worldToTrack.get(world.getName());
    }

    public RotationTrack getTrackById(String id) {
        return tracks.get(id);
    }

    public Collection<RotationTrack> getTracks() {
        return tracks.values();
    }
}
