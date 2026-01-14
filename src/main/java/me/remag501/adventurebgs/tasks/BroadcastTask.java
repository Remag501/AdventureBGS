package me.remag501.adventurebgs.tasks;

import me.clip.placeholderapi.PlaceholderAPI;
import me.remag501.adventurebgs.AdventureBGS;
import me.remag501.adventurebgs.AdventureSettings;
import me.remag501.adventurebgs.managers.PenaltyManager;
import me.remag501.adventurebgs.managers.RotationManager;
import me.remag501.adventurebgs.model.RotationTrack;
import me.remag501.adventurebgs.model.WorldInfo;
import me.remag501.adventurebgs.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Rotation;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Consumer;

import java.util.List;

public class BroadcastTask implements Runnable {

    private final AdventureBGS plugin;
    private final RotationManager rotationManager;

    private AdventureSettings settings;
    private boolean hasBroadcasted = false;
    private BukkitRunnable warningTask;
    private BossBar warningBossBar;
    private Consumer<String> onTimeUp;

    public BroadcastTask(AdventureBGS plugin, RotationManager rotationManager, AdventureSettings settings) {
        this.plugin = plugin;
        this.rotationManager = rotationManager;
        this.settings = settings;
    }

    @Override
    public void run() {

        for (RotationTrack rotationTrack: rotationManager.getTracks()) { // Change: Loop through every track in rotation manager, use same logic

            long minutesLeft = rotationTrack.getMinutesUntilNextCycle();
            long warnMinutes = settings.getWarnMinutes();

            String currentMap = rotationTrack.getCurrentWorld().getChatName();
            String nextMap = rotationTrack.getNextWorld().getChatName();

            long secondsLeft = rotationTrack.getSecondsUntilNextCycle();
            long warnSeconds = warnMinutes * 60;

            // =======================
            // WARNING PHASE
            // =======================
            if (secondsLeft <= warnSeconds && secondsLeft > warnSeconds - 20 && !hasBroadcasted) {

                String msg = settings.getWarnMessage();
                String formattedMsg = MessageUtil.format(msg, currentMap, nextMap, minutesLeft);
                String broadcastMsg = MessageUtil.color(rotationTrack.getCurrentWorld().getChatName()) + " §fcloses in §c" + warnMinutes + " §fminutes...";
                Bukkit.broadcastMessage(formattedMsg);
                World currentWorld = Bukkit.getWorld(rotationTrack.getCurrentWorld().getId());

                // Make warning into a title bar
                if (currentWorld != null) {
                    for (Player player : currentWorld.getPlayers()) {
                        // sendTitle(title, subtitle, fadeIn, stay, fadeOut)
                        // Using the message as the main title, and an empty string for subtitle
                        player.sendTitle(
                                broadcastMsg,
                                "",
                                10, // 0.5s fade in
                                70, // 3.5s stay
                                20  // 1.0s fade out
                        );
                    }
                }

                // Run next-world commands
                runWorldCommands(null, rotationTrack.getNextWorld());

                startWarningCountdown(rotationTrack);
                hasBroadcasted = true;
            }

            // =======================
            // ROTATION OCCURRED
            // =======================
            if (rotationTrack.isNewCycle()) {

                String msg = settings.getNewMapMessage();
                Bukkit.broadcastMessage(MessageUtil.format(msg, currentMap, nextMap, 0));

                stopWarningCountdown();
                hasBroadcasted = false;
            }

        }
    }

    // A simple setter to tell the task what to do when it finishes
    public void setOnTimeUp(Consumer<String> onTimeUp) {
        this.onTimeUp = onTimeUp;
    }
    private void runWorldCommands(Player player, WorldInfo world) {
        // 1. Get the raw list from the object
        List<String> rawCommands = world.getCommands();

        for (String rawCommand : rawCommands) {
            // 2. Parse the placeholders RIGHT NOW
            String processedCommand = PlaceholderAPI.setPlaceholders(player, rawCommand);

            // 3. Execute the command
            // Note: Use dispatchCommand for console or player.chat() for player
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
        }
    }

    // =======================
    // WARNING COUNTDOWN
    // =======================
    private void startWarningCountdown(RotationTrack rotation) {

        long totalSeconds = rotation.getSecondsUntilNextCycle();

        WorldInfo worldInfo = rotation.getCurrentWorld();
        World world = Bukkit.getWorld(worldInfo.getId());
        if (world == null) return;

        warningBossBar = Bukkit.createBossBar(
                "Map closes in " + totalSeconds + "s",
                BarColor.RED,
                BarStyle.SOLID
        );

        // Add existing players
        world.getPlayers().forEach(warningBossBar::addPlayer);

        warningTask = new BukkitRunnable() {
            long timeLeft = totalSeconds;

            @Override
            public void run() {

                if (timeLeft <= 0 || rotation.isNewCycle()) {
                    // Apply penalty to OLD map
                    stopWarningCountdown();
                    return;
                }

                if (timeLeft > 180 )
                    warningBossBar.hide();
                else if (timeLeft >= 120) {
                    warningBossBar.show();
                    warningBossBar.setTitle("§c§lWARNING: §cExtraction closes in §c§l2 §cminutes...");
                    warningBossBar.setProgress(1);
//                    warningBossBar.setProgress(Math.max(0.0, (double) timeLeft / totalSeconds));
                } else if (timeLeft >= 60) {
                    warningBossBar.show();
                    warningBossBar.setTitle("§c§lWARNING: §cExtraction closes in §c§l1 §cminute...");
                    warningBossBar.setProgress(1);
//                    warningBossBar.setProgress(Math.max(0.0, (double) timeLeft / totalSeconds));
                } else {
                    warningBossBar.show();
                    warningBossBar.setTitle("§c§lWARNING: §cExtraction closes in §c§l" + timeLeft + " §cseconds...");
                    warningBossBar.setProgress(1);
//                    warningBossBar.setProgress(Math.max(0.0, (double) timeLeft / totalSeconds));
                }

                timeLeft--;
            }
        };

        warningTask.runTaskTimer(plugin, 0L, 20L);

        // Apply penalty after warning ticks
        new BukkitRunnable() {
            @Override
            public void run() {
                if (onTimeUp != null) {
                    onTimeUp.accept(world.getName());
                }
            }
        }.runTaskLater(plugin, totalSeconds * 20L);

    }

    private void stopWarningCountdown() {
        if (warningTask != null) {
            warningTask.cancel();
            warningTask = null;
        }

        if (warningBossBar != null) {
            warningBossBar.removeAll();
            warningBossBar = null;
        }
    }

    // =======================
    // ACCESS FOR LISTENER
    // =======================
    public BossBar getWarningBossBar() {
        return warningBossBar;
    }
}
