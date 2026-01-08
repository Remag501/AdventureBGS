package me.remag501.adventurebgs.managers;

import me.remag501.adventurebgs.AdventureSettings;
import me.remag501.adventurebgs.model.ExtractionZone;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class ExtractionManager {

    private final AdventureSettings settings;

    public ExtractionManager(AdventureSettings settings) {
        this.settings = settings;
    }

    public ExtractionZone getZone(Location loc) {
        List<ExtractionZone> zones = settings.getExtractionZones().get(loc.getWorld().getName());
        if (zones == null) return null;

        for (ExtractionZone zone : zones) {
            if (zone.contains(loc)) {
                return zone;
            }
        }
        return null;
    }

    public boolean isInPortal(Location loc) {
        List<ExtractionZone> zones = settings.getExtractionZones().get(loc.getWorld().getName());
        if (zones == null) return false;
        return zones.stream().anyMatch(zone -> zone.inPortal(loc));
    }

    public List<ExtractionZone> getZones(String world) {
        return settings.getExtractionZones().getOrDefault(world, Collections.emptyList());
    }

    private int[] toIntArray(List<?> list) {
        if (list == null || list.size() < 3) return new int[]{0, 0, 0};
        return list.stream().mapToInt(o -> ((Number) o).intValue()).toArray();
    }

    private List<Double> toDoubleList(List<?> list) {
        if (list == null || list.isEmpty()) return List.of();
        List<Double> result = new ArrayList<>();
        for (Object o : list) {
            if (o instanceof Number num) result.add(num.doubleValue());
        }
        return result;
    }
}
