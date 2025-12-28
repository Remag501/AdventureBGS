package me.remag501.adventurebgs;

import me.remag501.adventurebgs.commands.AdventureCommand;
import me.remag501.adventurebgs.listeners.BroadcastListener;
import me.remag501.adventurebgs.listeners.ExtractionListener;
import me.remag501.adventurebgs.listeners.GuiListener;
import me.remag501.adventurebgs.listeners.JoinListener;
import me.remag501.adventurebgs.managers.*;
import me.remag501.adventurebgs.placeholder.BGSExpansion;
import me.remag501.adventurebgs.tasks.BroadcastTask;
import me.remag501.adventurebgs.util.MessageUtil;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.Bukkit;
import org.bukkit.World;

public class AdventureBGS extends JavaPlugin {

    private RotationManager rotationManager;
    private GuiManager guiManager;
    private ExtractionManager extractionManager;
    private PenaltyManager penaltyManager;
    private WeatherManager weatherManager;
    private BroadcastTask broadcastTask;
    private DeathManager deathManager;
    private BGSExpansion bgsExpansion;


    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Initialize managers
        rotationManager = new RotationManager(this);
        broadcastTask = new BroadcastTask(this);
        guiManager = new GuiManager(this);
        extractionManager = new ExtractionManager(this);
        penaltyManager = new PenaltyManager(this, broadcastTask);
        weatherManager = new WeatherManager(this);
        deathManager = new DeathManager(this);

        // Preload all configured worlds
        preloadWorlds();

        // Register commands
        getCommand("adventure").setExecutor(new AdventureCommand(this));

        // Register Listener
        getServer().getPluginManager().registerEvents(new GuiListener(this), this);
        getServer().getPluginManager().registerEvents(new ExtractionListener(this), this);
        getServer().getPluginManager().registerEvents(new BroadcastListener(this), this);
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);

        // Start broadcasting messages
        penaltyManager.startBroadcastTask();

        // Register placeholder
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new BGSExpansion(this).register();
            getLogger().info("AdventureBGS Placeholders registered!");
        }
    }

    @Override
    public void onDisable() {
        // Cleanup if needed
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

    public BroadcastTask getBroadcastTask() {
        return broadcastTask;
    }

    public DeathManager getDeathManager() {
        return deathManager;
    }

    public RotationManager getRotationManager() {
        return rotationManager;
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

//    public B getPlaceholderService() {
//        return placeholderService;
//    }

    public WeatherManager getWeatherManager() {
        return weatherManager;
    }
}
