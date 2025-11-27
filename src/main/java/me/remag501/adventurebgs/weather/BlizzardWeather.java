package me.remag501.adventurebgs.weather;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.EnumSet;
import java.util.Set;

public class BlizzardWeather implements WeatherEffect {

//    private static final int FREEZE_INCREASE = 30;
    private static final int FREEZE_DECREASE = 50;

    private static final int DAMAGE_THRESHOLD = 200;
    private static final int MAX_FREEZE = 300;

    private static final Set<Material> HEAT_BLOCKS = EnumSet.of(
            Material.CAMPFIRE,
            Material.SOUL_CAMPFIRE,
            Material.TORCH,
            Material.LANTERN,
            Material.FIRE,
            Material.MAGMA_BLOCK,
            Material.LAVA
    );

    @Override
    public void start(World world) {
        world.setStorm(true);
        world.setThundering(false);

        for (Player player : world.getPlayers()) {
            player.sendMessage(ChatColor.AQUA +
                    "❄ A blizzard is moving in! Find heat or you will freeze!");
        }
    }

    @Override
    public void tick(World world) {
        for (Player player : world.getPlayers()) {

            if (isWarming(player)) {
                warmPlayer(player);
                continue;
            }

            freezePlayer(player);
            applyColdEffects(player);

            world.spawnParticle(
                    Particle.SNOWFLAKE,
                    player.getLocation(),
                    20,
                    1.5, 1.5, 1.5,
                    0.02
            );
        }
    }

    private void freezePlayer(Player player) {
//        player.sendMessage("player freeze ticks " + player.getFreezeTicks());
        int freeze = (int) Math.min(
                MAX_FREEZE,
                40 + player.getFreezeTicks()  * 1.5
        );
        if (freeze < 60)
            freeze = 60;
        player.setFreezeTicks(freeze);
    }

    private void warmPlayer(Player player) {
        int freeze = Math.max(
                0,
                player.getFreezeTicks() - FREEZE_DECREASE
        );
        player.setFreezeTicks(freeze);
    }

    private void applyColdEffects(Player player) {
        int freeze = player.getFreezeTicks();

        if (freeze >= DAMAGE_THRESHOLD) {
//            player.damage(1.0);

//            player.addPotionEffect(new PotionEffect(
//                    PotionEffectType.SLOW,
//                    60,
//                    1,
//                    true,
//                    false
//            ));

            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.WEAKNESS,
                    60,
                    0,
                    true,
                    false
            ));
        }
    }

    private boolean isWarming(Player player) {

        // Player is on fire
        if (player.getFireTicks() > 0) return true;

        // Heat blocks nearby
        Location loc = player.getLocation();
        for (int x = -2; x <= 2; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -2; z <= 2; z++) {
                    Block block = loc.getBlock().getRelative(x, y, z);
                    if (HEAT_BLOCKS.contains(block.getType())) {
                        return true;
                    }
                }
            }
        }

        // Nearby burning entities
        for (Entity entity : player.getNearbyEntities(3, 3, 3)) {
            if (entity.getFireTicks() > 0) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void stop(World world) {
        world.setStorm(false);

        for (Player player : world.getPlayers()) {
            player.sendMessage(ChatColor.GREEN +
                    "The blizzard subsides. You feel warmer.");
        }
    }
}
