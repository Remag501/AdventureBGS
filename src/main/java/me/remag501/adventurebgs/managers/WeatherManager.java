package me.remag501.adventurebgs.managers;

import me.remag501.adventurebgs.AdventureBGS;
import me.remag501.adventurebgs.model.WeatherModel;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class WeatherManager {

    private final AdventureBGS plugin;
    private final Random random = new Random();

    // World -> weather entries
    private final Map<String, List<WeatherModel>> weatherByWorld = new HashMap<>();

    // Active weather tasks per world
    private final Map<String, BukkitRunnable> activeWeatherTasks = new HashMap<>();

    public WeatherManager(AdventureBGS plugin) {
        this.plugin = plugin;
        loadWeatherConfig();
        startSchedulers();
    }

    private void loadWeatherConfig() {
        List<Map<?, ?>> list = plugin.getConfig().getMapList("weather");

        for (Map<?, ?> map : list) {
            String type = (String) map.get("type");
            String world = (String) map.get("world");

            int minDuration = (int) map.get("min_duration");
            int maxDuration = (int) map.get("max_duration");

            int minFrequency = (int) map.get("min_frequency") * 60;
            int maxFrequency = (int) map.get("max_frequency") * 60;

            WeatherModel model = new WeatherModel(
                    type,
                    world,
                    minDuration,
                    maxDuration,
                    minFrequency,
                    maxFrequency
            );

            weatherByWorld
                    .computeIfAbsent(world, k -> new ArrayList<>())
                    .add(model);
        }
    }

    private void startSchedulers() {
        for (Map.Entry<String, List<WeatherModel>> entry : weatherByWorld.entrySet()) {
            String world = entry.getKey();
            scheduleNextWeather(world);
        }
    }

    private void scheduleNextWeather(String world) {
        List<WeatherModel> models = weatherByWorld.get(world);
        if (models == null || models.isEmpty()) return;

        WeatherModel model = models.get(random.nextInt(models.size()));
        int delaySeconds = randomBetween(
                model.getMinFrequencySeconds(),
                model.getMaxFrequencySeconds()
        );

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                startWeather(model);
            }
        };

        task.runTaskLater(plugin, delaySeconds * 20L);
        activeWeatherTasks.put(world, task);
    }

    private void startWeather(WeatherModel model) {
        String world = model.getWorld();

        // ✅ Hook later: start blizzard effects
        Bukkit.getLogger().info("[Weather] Starting " + model.getType() + " in " + world);

        int duration = randomBetween(
                model.getMinDurationSeconds(),
                model.getMaxDurationSeconds()
        );

        new BukkitRunnable() {
            @Override
            public void run() {
                stopWeather(model);
            }
        }.runTaskLater(plugin, duration * 20L);
    }

    private void stopWeather(WeatherModel model) {
        String world = model.getWorld();

        // ✅ Hook later: stop blizzard effects
        Bukkit.getLogger().info("[Weather] Stopping " + model.getType() + " in " + world);

        // Schedule next occurrence
        scheduleNextWeather(world);
    }

    private int randomBetween(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }
}