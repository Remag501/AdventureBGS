package me.remag501.adventurebgs;

import me.remag501.adventurebgs.commands.AdventureCommand;
import me.remag501.adventurebgs.listeners.GuiListener;
import me.remag501.adventurebgs.managers.GuiManager;
import me.remag501.adventurebgs.managers.RotationManager;
import me.remag501.adventurebgs.managers.TimeManager;
import me.remag501.adventurebgs.util.MessageUtil;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.Bukkit;
import org.bukkit.World;

public class AdventureBGS extends JavaPlugin {

    private RotationManager rotationManager;
    private TimeManager timeManager;
    private GuiManager guiManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Initialize managers
        rotationManager = new RotationManager(this);
        timeManager = new TimeManager(this);
        guiManager = new GuiManager(this);

        // Preload all configured worlds
        preloadWorlds();

        // Register commands
        getCommand("adventure").setExecutor(new AdventureCommand(this));

        // Register Listener
        getServer().getPluginManager().registerEvents(new GuiListener(), this);

        // Start broadcasting messages
        timeManager.startBroadcastTask();
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

    public GuiManager getGuiManager() {
        return guiManager;
    }
}
