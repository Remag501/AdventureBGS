package me.remag501.adventurebgs.weather;

import org.bukkit.World;

public interface WeatherEffect {

    void start(World world);

    void tick(World world);

    void stop(World world);
}
