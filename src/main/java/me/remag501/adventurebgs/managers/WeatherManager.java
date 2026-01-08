package me.remag501.adventurebgs.managers;

import me.remag501.adventurebgs.AdventureBGS;
import me.remag501.adventurebgs.AdventureSettings;
import me.remag501.adventurebgs.model.WeatherModel;
import me.remag501.adventurebgs.weather.BlizzardWeather;
import me.remag501.adventurebgs.weather.WeatherEffect;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WeatherManager {

    // Stores a map of all weather types to classes
    private static final Map<String, WeatherEffect> WEATHER_REGISTRY = Map.of(
            "blizzard", new BlizzardWeather()
    );

    private final AdventureBGS plugin;
    private final List<WeatherModel> weathers;

    public WeatherManager(AdventureBGS plugin) {
        this.plugin = plugin;
        AdventureSettings settings = plugin.getSettings();
        weathers = settings.getWeatherModels();
        startScheduling();
    }


    private void startScheduling() {
        for (WeatherModel model : weathers) {
            scheduleNext(model);
        }
    }

    private void scheduleNext(WeatherModel model) {
        int delayTicks = model.randomFrequencyMinutes() * 60 * 20; // Won't wait for previous weather to finish up

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            startWeather(model);
            scheduleNext(model); // chain forever
        }, delayTicks);
    }

    private void startWeather(WeatherModel model) {
        World world = Bukkit.getWorld(model.getWorld());
        if (world == null) return;

        WeatherEffect effect = WEATHER_REGISTRY.get(model.getType().toLowerCase());
        if (effect == null) return;

        int durationTicks = model.randomDurationSeconds() * 20;

        effect.start(world);
        plugin.getLogger().info("Starting weather " + model.getType() + " for " + (durationTicks / 20) + " seconds in world " + world.getName() + ".");

        BukkitRunnable tickTask = new BukkitRunnable() {
            @Override
            public void run() {
                effect.tick(world);
            }
        };

        tickTask.runTaskTimer(plugin, 0L, 20L);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            tickTask.cancel();
            effect.stop(world);
        }, durationTicks);
    }
}
