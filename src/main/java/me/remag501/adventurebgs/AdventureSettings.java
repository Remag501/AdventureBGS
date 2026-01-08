package me.remag501.adventurebgs;

import me.remag501.adventurebgs.model.ExtractionZone;
import me.remag501.adventurebgs.model.WeatherModel;
import me.remag501.adventurebgs.model.WorldInfo;
import org.bukkit.configuration.file.FileConfiguration;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.time.Instant;
import java.util.*;

public class AdventureSettings {

    // Rotation
    private final int cycleMinutes;
    private final Instant startCycle;
    private final List<WorldInfo> worlds = new ArrayList<>();

    // Broadcast
    private final int warnMinutes;
    private final String warnMessage;
    private final String newMapMessage;

    // Penalty
    private final String penaltyMessage;
    private final String penaltySound;

    // Extraction
    private final int extractionDuration;
    private final int portalOpenSeconds;
    private final int downSeconds;
    private final Location extractionSpawn;
    private final String extractionStart;
    private final String extractionBossTitle;
    private final String extractionSuccess;
    private final String extractionPortalOpen;
    private final String extractionDown;
    private final String extractionCancel;
//    private final Map<String, String> extractionMessages = new HashMap<>();
    private final Map<String, List<ExtractionZone>> extractionZones = new HashMap<>();

    // Alert Settings
    private final int alertSeconds;
    private final boolean alertParticles;
    private final boolean alertFireworks;
    private final String alertSound;
    private final boolean alertUsePlayerLoc;
    private final Location alertFixedLocation;

    // GUI
    private final String guiTitle;
    private final int guiTeleportSlot;
    private final String guiTeleportName;
    private final int guiInfoSlot;
    private final String guiInfoName;
    private final List<String> guiInfoLore;

    // Weather
    private final List<WeatherModel> weatherModels = new ArrayList<>();

    public AdventureSettings(FileConfiguration config) {
        // --- Rotation ---
        this.cycleMinutes = config.getInt("rotation.cycle-minutes");
        this.startCycle = Instant.parse(config.getString("rotation.start-cycle", "2025-01-01T00:00:00Z"));

        List<Map<?, ?>> worldsList = config.getMapList("rotation.worlds");

        if (worldsList != null && !worldsList.isEmpty()) {
            for (Map<?, ?> map : worldsList) {
                // Since it's a list of maps, we pull values directly from the map
                String id = (String) map.get("id");
                String texture = (String) map.get("texture");
                String chatName = color((String) map.get("chat-name"));
                String guiName = color((String) map.get("gui-name"));

                // Handle Lists safely
                List<String> lore = colorList((List<String>) map.get("lore"));
                List<String> commands = (List<String>) map.get("commands");

                worlds.add(new WorldInfo(id, texture, chatName, guiName, lore, commands));
                Bukkit.getLogger().info("Successfully loaded world: " + id);
            }
        } else {
            Bukkit.getLogger().warning("Rotation worlds list is null or empty!");
        }

        // --- Broadcast & Penalty ---
        this.warnMinutes = config.getInt("broadcast.warn-minutes");
        this.warnMessage = color(config.getString("broadcast.warn-message"));
        this.newMapMessage = color(config.getString("broadcast.new-map-message"));
        this.penaltyMessage = color(config.getString("penalty.message"));
        this.penaltySound = config.getString("penalty.sound");

        // --- Extraction ---
        this.extractionDuration = config.getInt("extraction.duration");
        this.portalOpenSeconds = config.getInt("extraction.portal-open-seconds");
        this.downSeconds = config.getInt("extraction.down-seconds");
        this.extractionSpawn = new Location(
                Bukkit.getWorld(config.getString("extraction.spawn.world")),
                config.getDouble("extraction.spawn.x"),
                config.getDouble("extraction.spawn.y"),
                config.getDouble("extraction.spawn.z")
        );

        // Extraction messages
        this.extractionStart = config.getString("extraction.message.start");
        this.extractionBossTitle = config.getString("extraction.message.boss-title");
        this.extractionSuccess = config.getString("extraction.message.success");
        this.extractionPortalOpen = config.getString("extraction.message.portal-open");
        this.extractionDown = config.getString("extraction.message.down");
        this.extractionCancel = config.getString("extraction.message.cancel");

        // Parse Extraction Zones
        ConfigurationSection zonesSec = config.getConfigurationSection("extraction.zones");
        if (zonesSec != null) {
            for (String worldKey : zonesSec.getKeys(false)) {
                List<ExtractionZone> zonesForWorld = new ArrayList<>();
                List<Map<?, ?>> zoneList = config.getMapList("extraction.zones." + worldKey);

                for (Map<?, ?> map : zoneList) {
                    zonesForWorld.add(parseZone(worldKey, map));
                }
                extractionZones.put(worldKey, zonesForWorld);
            }
        }

        // Extraction Alert
        this.alertSeconds = config.getInt("extraction.alert-seconds");
        this.alertParticles = config.getBoolean("extraction.alert.particles");
        this.alertFireworks = config.getBoolean("extraction.alert.fireworks");
        this.alertSound = config.getString("extraction.alert.sound");
        this.alertUsePlayerLoc = config.getBoolean("extraction.alert.location.use-player");
        this.alertFixedLocation = new Location(
                Bukkit.getWorld(config.getString("extraction.alert.location.world")),
                config.getDouble("extraction.alert.location.x"),
                config.getDouble("extraction.alert.location.y"),
                config.getDouble("extraction.alert.location.z")
        );

        // --- GUI ---
        this.guiTitle = color(config.getString("gui.title"));
        this.guiTeleportSlot = config.getInt("gui.teleport.slot");
        this.guiTeleportName = color(config.getString("gui.teleport.name"));
        this.guiInfoSlot = config.getInt("gui.info.slot");
        this.guiInfoName = color(config.getString("gui.info.name"));
        this.guiInfoLore = colorList(config.getStringList("gui.info.lore"));

        // --- Weather ---
        List<Map<?, ?>> weatherList = config.getMapList("weather");
        for (Map<?, ?> entry : weatherList) {
            weatherModels.add(new WeatherModel(
                    (String) entry.get("type"),
                    (String) entry.get("world"),
                    (int) entry.get("min_duration"),
                    (int) entry.get("max_duration"),
                    (int) entry.get("min_frequency"),
                    (int) entry.get("max_frequency")
            ));
        }
    }

    private ExtractionZone parseZone(String worldName, Map<?, ?> map) {
        int[] min = toIntArray(map.get("min"));
        int[] max = toIntArray(map.get("max"));

        // Handle optional portal/beacon data
        int[] pMin = map.containsKey("portal_min") ? toIntArray(map.get("portal_min")) : min;
        int[] pMax = map.containsKey("portal_max") ? toIntArray(map.get("portal_max")) : max;

        List<Double> beacon = toDoubleList(map.get("beacon_loc"));
        List<Double> particle = toDoubleList(map.get("particle_loc"));

        return new ExtractionZone(worldName, min, max, pMin, pMax, beacon, particle);
    }

    // --- Helpers ---

    private String color(String s) {
        return s == null ? "" : ChatColor.translateAlternateColorCodes('&', s);
    }

    private List<String> colorList(List<String> list) {
        list.replaceAll(this::color);
        return list;
    }

    private int[] toIntArray(Object obj) {
        if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            return new int[]{((Number) list.get(0)).intValue(), ((Number) list.get(1)).intValue(), ((Number) list.get(2)).intValue()};
        }
        return new int[]{0, 0, 0};
    }

    private List<Double> toDoubleList(Object obj) {
        if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            return Arrays.asList(((Number) list.get(0)).doubleValue(), ((Number) list.get(1)).doubleValue(), ((Number) list.get(2)).doubleValue());
        }
        return null;
    }

    // --- Getters ---
    public List<WorldInfo> getWorlds() { return worlds; }
    public List<WeatherModel> getWeatherModels() { return weatherModels; }
    public List<ExtractionZone> getZonesForWorld(String worldId) { return extractionZones.getOrDefault(worldId, Collections.emptyList()); }

    // Remaining getters

    public int getCycleMinutes() {
        return cycleMinutes;
    }

    public Instant getStartCycle() {
        return startCycle;
    }

    public int getWarnMinutes() {
        return warnMinutes;
    }

    public String getWarnMessage() {
        return warnMessage;
    }

    public String getNewMapMessage() {
        return newMapMessage;
    }

    public String getPenaltyMessage() {
        return penaltyMessage;
    }

    public String getPenaltySound() {
        return penaltySound;
    }

    public int getExtractionDuration() {
        return extractionDuration;
    }

    public Location getExtractionSpawn() {
        return extractionSpawn;
    }

    public String getExtractionStart() {
        return extractionStart;
    }

    public String getExtractionBossTitle() {
        return extractionBossTitle;
    }

    public String getExtractionSuccess() {
        return extractionSuccess;
    }

    public String getExtractionPortalOpen() {
        return extractionPortalOpen;
    }

    public String getExtractionDown() {
        return extractionDown;
    }

    public String getExtractionCancel() {
        return extractionCancel;
    }

    public Map<String, List<ExtractionZone>> getExtractionZones() {
        return extractionZones;
    }

    public int getAlertSeconds() {
        return alertSeconds;
    }

    public boolean isAlertParticles() {
        return alertParticles;
    }

    public boolean isAlertFireworks() {
        return alertFireworks;
    }

    public String getAlertSound() {
        return alertSound;
    }

    public boolean isAlertUsePlayerLoc() {
        return alertUsePlayerLoc;
    }

    public Location getAlertFixedLocation() {
        return alertFixedLocation;
    }

    public String getGuiTitle() {
        return guiTitle;
    }

    public int getGuiTeleportSlot() {
        return guiTeleportSlot;
    }

    public String getGuiTeleportName() {
        return guiTeleportName;
    }

    public int getGuiInfoSlot() {
        return guiInfoSlot;
    }

    public String getGuiInfoName() {
        return guiInfoName;
    }

    public List<String> getGuiInfoLore() {
        return guiInfoLore;
    }

    public int getPortalOpenSeconds() {
        return portalOpenSeconds;
    }

    public int getDownSeconds() {
        return downSeconds;
    }

}
