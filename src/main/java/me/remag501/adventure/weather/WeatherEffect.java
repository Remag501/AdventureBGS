package me.remag501.adventure.weather;

import org.bukkit.World;

public interface WeatherEffect {

    void start(World world);

    void tick(World world);

    void stop(World world);
}
