package me.remag501.adventurebgs;

import me.remag501.adventurebgs.commands.AdventureCommand;
import me.remag501.adventurebgs.listeners.GuiListener;
import me.remag501.adventurebgs.managers.RotationManager;
import me.remag501.adventurebgs.util.MessageUtil;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.Bukkit;
import org.bukkit.World;

public class AdventureBGS extends JavaPlugin {

    private RotationManager rotationManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Initialize rotation manager
        rotationManager = new RotationManager(this);

        // Preload all configured worlds
        preloadWorlds();

        // Register commands
        getCommand("adventure").setExecutor(new AdventureCommand(this));

        // Register Listener
        getServer().getPluginManager().registerEvents(new GuiListener(), this);

        // Start broadcasting messages
        startBroadcastTask();
    }

    @Override
    public void onDisable() {
        // Cleanup if needed
    }

    public RotationManager getRotationManager() {
        return rotationManager;
    }

    private void preloadWorlds() {
        for (String worldName : rotationManager.getWorlds()) {
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                getLogger().warning("World not loaded: " + worldName);
            } else {
                getLogger().info("Preloaded world: " + worldName);
            }
        }
    }

    public void reloadPluginConfig() {
        reloadConfig(); // Reloads config.yml
        rotationManager = new RotationManager(this); // Reinitialize with new data
    }

    private void startBroadcastTask() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            RotationManager rotation = getRotationManager();

            long minutesLeft = rotation.getMinutesUntilNextCycle();
            String currentMap = rotation.getCurrentWorld().getName();
            String nextMap = rotation.getNextWorld().getName();

            // Warning
            long warnMinutes = getConfig().getLong("broadcast.warn-minutes");
            if (minutesLeft == warnMinutes) {
                String msg = getConfig().getString("broadcast.warn-message");
                Bukkit.broadcastMessage(MessageUtil.format(msg, currentMap, nextMap, minutesLeft));
            }

            // New map event (detect cycle boundary)
            if (rotation.isNewCycle()) {
                String msg = getConfig().getString("broadcast.new-map-message");
                Bukkit.broadcastMessage(MessageUtil.format(msg, currentMap, nextMap, minutesLeft));
            }

        }, 20L, 20L); // Run every second
    }
}
