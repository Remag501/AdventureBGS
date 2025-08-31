package me.remag501.adventurebgs;

import me.remag501.adventurebgs.commands.AdventureCommand;
import me.remag501.adventurebgs.listeners.ExtractionListener;
import me.remag501.adventurebgs.listeners.GuiListener;
import me.remag501.adventurebgs.managers.*;
import me.remag501.adventurebgs.util.MessageUtil;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.Bukkit;
import org.bukkit.World;

public class AdventureBGS extends JavaPlugin {

    private RotationManager rotationManager;
    private GuiManager guiManager;
    private ExtractionManager extractionManager;
    private PenaltyManager penaltyManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Initialize managers
        rotationManager = new RotationManager(this);
        guiManager = new GuiManager(this);
        extractionManager = new ExtractionManager(this);
        penaltyManager = new PenaltyManager(this);

        // Preload all configured worlds
        preloadWorlds();

        // Register commands
        getCommand("adventure").setExecutor(new AdventureCommand(this));

        // Register Listener
        getServer().getPluginManager().registerEvents(new GuiListener(this), this);
        getServer().getPluginManager().registerEvents(new ExtractionListener(this), this);

        // Start broadcasting messages
        penaltyManager.startBroadcastTask();
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
        reloadConfig(); // Reloads config.yml for spigot
        rotationManager = new RotationManager(this);
        extractionManager = new ExtractionManager(this);
//        guiManager = new GuiManager(this); // Empty constructor, no need for re init
//        penaltyManager = new PenaltyManager(this); // No config in constructor, no need for re init
    }

    public PenaltyManager getPenaltyManager() {
        return penaltyManager;
    }

    public ExtractionManager getExtractionManager() {
        return extractionManager;
    }

    public GuiManager getGuiManager() {
        return guiManager;
    }
}
