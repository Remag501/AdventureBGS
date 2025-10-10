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
        if (section == null) return;

        for (String worldKey : section.getKeys(false)) {
            List<Map<?, ?>> zoneList = plugin.getConfig().getMapList("extraction.zones." + worldKey);
            List<ExtractionZone> zones = new ArrayList<>();

            for (Map<?, ?> map : zoneList) {
                try {
                    int[] min = toIntArray((List<?>) map.get("min"));
                    int[] max = toIntArray((List<?>) map.get("max"));
                    int[] portalMin = (map.get("portal_min") instanceof List<?>) ?
                            toIntArray((List<?>) map.get("portal_min")) : new int[]{0, 0, 0};
                    int[] portalMax = (map.get("portal_max") instanceof List<?>) ?
                            toIntArray((List<?>) map.get("portal_max")) : new int[]{0, 0, 0};
                    List<Double> beaconLoc = toDoubleList((List<?>) map.get("beacon_loc"));
                    List<Double> particleLoc = toDoubleList((List<?>) map.get("particle_loc"));

                    zones.add(new ExtractionZone(worldKey, min, max, portalMin, portalMax, beaconLoc, particleLoc));
                } catch (Exception e) {
                    plugin.getLogger().warning("[AdventureBGS] Failed to load extraction zone in world '" + worldKey + "': " + e.getMessage());
                }
            }

            zonesPerWorld.put(worldKey, zones);
        }
    }

    public boolean isInAnyZone(Location loc) {
        List<ExtractionZone> zones = zonesPerWorld.get(loc.getWorld().getName());
        if (zones == null) return false;
        return zones.stream().anyMatch(zone -> zone.contains(loc));
    }

    public boolean isInPortal(Location loc) {
        List<ExtractionZone> zones = zonesPerWorld.get(loc.getWorld().getName());
        if (zones == null) return false;
        return zones.stream().anyMatch(zone -> zone.inPortal(loc));
    }

    public List<ExtractionZone> getZones(String world) {
        return zonesPerWorld.getOrDefault(world, Collections.emptyList());
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
