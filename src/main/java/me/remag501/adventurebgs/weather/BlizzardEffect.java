package me.remag501.adventurebgs.weather;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class BlizzardWeather implements WeatherEffect {

    @Override
    public void start(World world) {
        world.setStorm(true);
        world.setThundering(false);
    }

    @Override
    public void tick(World world) {
        for (Player player : world.getPlayers()) {
            player.damage(1.0);

            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.SLOW,
                    60,
                    2,
                    true,
                    false
            ));

            world.spawnParticle(
                    Particle.SNOWFLAKE,
                    player.getLocation(),
                    20,
                    1.5, 1.5, 1.5,
                    0.02
            );
        }
    }

    @Override
    public void stop(World world) {
        world.setStorm(false);
    }
}
