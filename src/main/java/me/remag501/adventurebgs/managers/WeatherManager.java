package me.remag501.adventurebgs.managers;

import me.remag501.adventurebgs.AdventureBGS;
import me.remag501.adventurebgs.model.WeatherModel;
import me.remag501.adventurebgs.weather.BlizzardEffect;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WeatherManager {

    private final AdventureBGS plugin;
    private final List<WeatherModel> weathers = new ArrayList<>();

    public WeatherManager(AdventureBGS plugin) {
        this.plugin = plugin;
        loadWeather();
        startScheduling();
    }

    private void loadWeather() {
        List<Map<?, ?>> list = plugin.getConfig().getMapList("weather");

        for (Map<?, ?> map : list) {
            WeatherModel model = new WeatherModel(
                    (String) map.get("type"),
                    (String) map.get("world"),
                    (int) map.get("min_duration"),
                    (int) map.get("max_duration"),
                    (int) map.get("min_frequency"),
                    (int) map.get("max_frequency")
            );
            weathers.add(model);
        }
    }

    private void startScheduling() {
        for (WeatherModel model : weathers) {
            scheduleNext(model);
        }
    }

    private void scheduleNext(WeatherModel model) {
        int delayTicks = model.randomFrequencyMinutes() * 60 * 20;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            startWeather(model);
            scheduleNext(model); // chain forever
        }, delayTicks);
    }

    private void startWeather(WeatherModel model) {
        World world = Bukkit.getWorld(model.getWorld());
        if (world == null) return;

        if (model.getType().equalsIgnoreCase("blizzard")) {
            startBlizzard(model, world);
        }
    }

    private void startBlizzard(WeatherModel model, World world) {
        int durationTicks = model.randomDurationSeconds() * 20;

        new BukkitRunnable() {
            @Override
            public void run() {
                BlizzardEffect.tick(world);
            }
        }.runTaskTimer(plugin, 0L, 40L); // every 2s

        Bukkit.getScheduler().runTaskLater(plugin,
                () -> BlizzardEffect.stop(world),
                durationTicks
        );
    }
}
