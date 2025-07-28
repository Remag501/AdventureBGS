package me.remag501.adventurebgs.managers;

import me.remag501.adventurebgs.model.ExtractionZone;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class ExtractionManager {
    private final Map<String, List<ExtractionZone>> zonesPerWorld = new HashMap<>();

    public ExtractionManager(JavaPlugin plugin) {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("extraction.zones");
        if (section != null) {
            for (String worldKey : section.getKeys(false)) {
                List<Map<?, ?>> zoneList = plugin.getConfig().getMapList("extraction.zones." + worldKey);
                List<ExtractionZone> zones = new ArrayList<>();
                for (Map<?, ?> map : zoneList) {
                    int[] min = ((List<Integer>) map.get("min")).stream().mapToInt(Integer::intValue).toArray();
                    int[] max = ((List<Integer>) map.get("max")).stream().mapToInt(Integer::intValue).toArray();
                    zones.add(new ExtractionZone(worldKey, min, max));
                }
                zonesPerWorld.put(worldKey, zones);
            }
        }
    }

    public boolean isInAnyZone(Location loc) {
        List<ExtractionZone> zones = zonesPerWorld.get(loc.getWorld().getName());
        if (zones == null) return false;
        return zones.stream().anyMatch(zone -> zone.contains(loc));
    }
}



