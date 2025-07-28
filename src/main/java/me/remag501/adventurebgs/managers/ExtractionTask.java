package me.remag501.adventurebgs.managers;

import me.remag501.adventurebgs.AdventureBGS;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ExtractionTask implements Runnable {

    private final AdventureBGS plugin;
    private final ExtractionManager manager;
    private final Map<UUID, Integer> extracting = new HashMap<>();
    private final Map<UUID, BossBar> bossBars = new HashMap<>();
    private final int extractionDuration;

    public ExtractionTask(AdventureBGS plugin, ExtractionManager manager) {
        this.plugin = plugin;
        this.manager = manager;
        this.extractionDuration = plugin.getConfig().getInt("extraction.duration");
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            boolean inZone = manager.isInAnyZone(player.getLocation());

            if (inZone) {
                extracting.putIfAbsent(player.getUniqueId(), extractionDuration);

                int timeLeft = extracting.get(player.getUniqueId());
                if (!bossBars.containsKey(player.getUniqueId())) {
                    BossBar bar = Bukkit.createBossBar(
                            ChatColor.YELLOW + "Extracting...",
                            BarColor.GREEN,
                            BarStyle.SOLID
                    );
                    bar.addPlayer(player);
                    bossBars.put(player.getUniqueId(), bar);
                }

                BossBar bar = bossBars.get(player.getUniqueId());
                bar.setProgress((double) timeLeft / extractionDuration);
                bar.setTitle(ChatColor.YELLOW + "Extracting... " + timeLeft + "s");

                if (timeLeft <= 1) {
                    completeExtraction(player);
                    continue;
                }

                extracting.put(player.getUniqueId(), timeLeft - 1);

            } else if (extracting.containsKey(player.getUniqueId())) {
                cancelExtraction(player);
            }
        }
    }

    private void completeExtraction(Player player) {
        extracting.remove(player.getUniqueId());
        BossBar bar = bossBars.remove(player.getUniqueId());
        if (bar != null) bar.removeAll();

        player.sendMessage(ChatColor.GREEN + plugin.getConfig().getString("extraction.message.success"));

        // Teleport to spawn
        String world = plugin.getConfig().getString("extraction.spawn.world");
        double x = plugin.getConfig().getDouble("extraction.spawn.x");
        double y = plugin.getConfig().getDouble("extraction.spawn.y");
        double z = plugin.getConfig().getDouble("extraction.spawn.z");

        World spawnWorld = Bukkit.getWorld(world);
        if (spawnWorld != null) {
            player.teleport(new Location(spawnWorld, x, y, z));
        }
    }

    private void cancelExtraction(Player player) {
        extracting.remove(player.getUniqueId());
        BossBar bar = bossBars.remove(player.getUniqueId());
        if (bar != null) bar.removeAll();

        player.sendMessage(ChatColor.RED + plugin.getConfig().getString("extraction.message.cancel"));
    }
}
