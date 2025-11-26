package me.remag501.adventurebgs.weather;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class BlizzardEffect {

    public static void tick(World world) {
        for (Player player : world.getPlayers()) {

            // Cold damage
            player.damage(1.0);

            // Heavy slow + vision punishment
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.SLOW,
                    60,
                    2,
                    true,
                    false
            ));

            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.BLINDNESS,
                    40,
                    0,
                    true,
                    false
            ));

            // Atmosphere (cheap)
            world.spawnParticle(
                    Particle.SNOWFLAKE,
                    player.getLocation(),
                    20,
                    1.5, 1.5, 1.5,
                    0.02
            );
        }
    }

    public static void stop(World world) {
        // Intentionally empty for now
        // Potion effects expire naturally
    }
}
