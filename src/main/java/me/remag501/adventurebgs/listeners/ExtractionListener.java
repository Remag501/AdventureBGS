package me.remag501.adventurebgs.listeners;

import me.remag501.adventurebgs.AdventureBGS;
import me.remag501.adventurebgs.model.ExtractionState;
import me.remag501.adventurebgs.managers.ExtractionManager;
import me.remag501.adventurebgs.util.MessageUtil;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ExtractionListener implements Listener {

    private final AdventureBGS plugin;
    private final ExtractionManager manager;
    private final Map<UUID, ExtractionState> activeExtractions = new HashMap<>();
    private final int extractionDuration;

    public ExtractionListener(AdventureBGS plugin) {
        this.plugin = plugin;
        this.manager = plugin.getExtractionManager();
        this.extractionDuration = plugin.getConfig().getInt("extraction.duration");
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // Ignore micro movements (only react when changing block coords)
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        // Ignore players not in extraction world
        if (!player.getWorld().getName().equals(plugin.getRotationManager().getCurrentWorld().getName()))
            return;

        boolean inZone = manager.isInAnyZone(player.getLocation());
        boolean extracting = activeExtractions.containsKey(player.getUniqueId());

        if (inZone && !extracting) {
            startExtraction(player);
        } else if (!inZone && extracting) {
            cancelExtraction(player, true);
        }
    }

    private void startExtraction(Player player) {
        player.sendMessage(MessageUtil.color(plugin.getConfig().getString("extraction.message.start")
                .replace("%seconds%", String.valueOf(extractionDuration))));

        String title = MessageUtil.color(plugin.getConfig().getString("extraction.message.title"));

        BossBar bossBar = Bukkit.createBossBar(
                title,
                BarColor.GREEN,
                BarStyle.SOLID
        );
        bossBar.addPlayer(player);

        BukkitRunnable task = new BukkitRunnable() {
            int timeLeft = extractionDuration;
            boolean alertTriggered = false;

            @Override
            public void run() {
                if (timeLeft <= 0) {
                    completeExtraction(player);
                    return;
                }

                // Alert other players X seconds before completion
                int alertSeconds = plugin.getConfig().getInt("extraction.alert-seconds", 3);
                if (timeLeft == alertSeconds && !alertTriggered) {
                    triggerAlert(player);
                    alertTriggered = true;
                }

                bossBar.setProgress((double) timeLeft / extractionDuration);
                String updatedTitle = title.replace("%seconds%", String.valueOf(timeLeft));
                bossBar.setTitle(updatedTitle);
                timeLeft--;
            }
        };


        task.runTaskTimer(plugin, 0L, 20L);
        activeExtractions.put(player.getUniqueId(), new ExtractionState(task, bossBar));
    }

    private void cancelExtraction(Player player, boolean notify) {
        ExtractionState state = activeExtractions.remove(player.getUniqueId());
        if (state != null) state.cancel();

        if (notify) {
            player.sendMessage(MessageUtil.color(plugin.getConfig().getString("extraction.message.cancel")));
        }
    }

    private void completeExtraction(Player player) {
        cancelExtraction(player, false);
        player.sendMessage(MessageUtil.color(plugin.getConfig().getString("extraction.message.success")));

        // Teleport to configured spawn
        String world = plugin.getConfig().getString("extraction.spawn.world");
        double x = plugin.getConfig().getDouble("extraction.spawn.x");
        double y = plugin.getConfig().getDouble("extraction.spawn.y");
        double z = plugin.getConfig().getDouble("extraction.spawn.z");

        World spawnWorld = Bukkit.getWorld(world);
        if (spawnWorld != null) {
            player.teleport(new Location(spawnWorld, x, y, z));
        }
    }

    private void triggerAlert(Player extractingPlayer) {
        boolean usePlayerLocation = plugin.getConfig().getBoolean("extraction.alert.location.use-player", true);
        Location loc;

        if (usePlayerLocation) {
            loc = extractingPlayer.getLocation();
        } else {
            World world = Bukkit.getWorld(plugin.getConfig().getString("extraction.alert.location.world"));
            double x = plugin.getConfig().getDouble("extraction.alert.location.x");
            double y = plugin.getConfig().getDouble("extraction.alert.location.y");
            double z = plugin.getConfig().getDouble("extraction.alert.location.z");
            loc = new Location(world, x, y, z);
        }

        // Fireworks
        if (plugin.getConfig().getBoolean("extraction.alert.fireworks", true)) {
            Firework firework = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
            FireworkMeta meta = firework.getFireworkMeta();
            meta.addEffect(FireworkEffect.builder()
                    .withColor(Color.RED)
                    .with(FireworkEffect.Type.BALL_LARGE)
                    .flicker(true)
                    .trail(true)
                    .build());
            meta.setPower(1);
            firework.setFireworkMeta(meta);
        }

        // Particles
        if (plugin.getConfig().getBoolean("extraction.alert.particles", true)) {
            loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc, 5, 1, 1, 1, 0.05);
        }

        // Sound
        String soundName = plugin.getConfig().getString("extraction.alert.sound", "ENTITY_ENDER_DRAGON_GROWL");
        Sound sound = Sound.valueOf(soundName);
        loc.getWorld().playSound(loc, sound, 1.0f, 1.0f);
    }

}

