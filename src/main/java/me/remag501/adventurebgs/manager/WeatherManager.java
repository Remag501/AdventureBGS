package me.remag501.adventurebgs.manager;

import me.remag501.adventurebgs.AdventureBGS;
import me.remag501.adventurebgs.setting.AdventureSettings;
import me.remag501.adventurebgs.model.WeatherModel;
import me.remag501.adventurebgs.weather.BlizzardWeather;
import me.remag501.adventurebgs.weather.WeatherEffect;
import me.remag501.bgscore.api.task.TaskService;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Map;

public class WeatherManager {

    // Stores a map of all weather types to classes
    private static final Map<String, WeatherEffect> WEATHER_REGISTRY = Map.of(
            "blizzard", new BlizzardWeather()
    );

    private final TaskService taskService;

    private List<WeatherModel> weathers;

    public WeatherManager(TaskService taskService, AdventureSettings settings) {
        this.taskService = taskService;
        weathers = settings.getWeatherModels();
        startScheduling();
    }

    public void reload(AdventureSettings settings) {
        this.weathers = settings.getWeatherModels();
    }


    private void startScheduling() {
        for (WeatherModel model : weathers) {
            scheduleNext(model);
        }
    }

    private void scheduleNext(WeatherModel model) {
        int delayTicks = model.randomFrequencyMinutes() * 60 * 20; // Won't wait for previous weather to finish up

        taskService.delay(delayTicks, () -> {
            startWeather(model);
            scheduleNext(model); // chain forever
        });

    }

    private void startWeather(WeatherModel model) {
        World world = Bukkit.getWorld(model.getWorld());
        if (world == null) return;

        WeatherEffect effect = WEATHER_REGISTRY.get(model.getType().toLowerCase());
        if (effect == null) return;

        int durationTicks = model.randomDurationSeconds() * 20;

        effect.start(world);
        Bukkit.getLogger().info("[Adventure] Starting weather " + model.getType() + " for " + (durationTicks / 20) + " seconds in world " + world.getName() + ".");

        taskService.subscribe(AdventureBGS.SYSTEM_ID, model.getType(), 0, 20, (ticks) -> {
            effect.tick(world);
            return false;
        });

        taskService.delay(durationTicks, () -> {
            taskService.stopTask(AdventureBGS.SYSTEM_ID, model.getType());
        });

    }
}
