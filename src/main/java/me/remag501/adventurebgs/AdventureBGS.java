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

    private AdventureSettings settings;
    private SettingsProvider provider;


    @Override
    public void onEnable() {
        // Config stuff
        saveDefaultConfig();
        this.provider = new SettingsProvider(this);
        this.settings = provider.getSettings();

        // Initialize managers
        rotationManager = new RotationManager(settings);
        deathManager = new DeathManager(this);
        extractionManager = new ExtractionManager(settings);
        broadcastTask = new BroadcastTask(this, rotationManager, penaltyManager, settings);
        penaltyManager = new PenaltyManager(this, broadcastTask, settings);
        weatherManager = new WeatherManager(this, settings);
        guiManager = new GuiManager(rotationManager, settings);

        // Preload all configured worlds
        preloadWorlds();

        // Register commands
        getCommand("adventure").setExecutor(new AdventureCommand(this));

        // Register Listener
        getServer().getPluginManager().registerEvents(new ExtractionListener(this), this);
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        getServer().getPluginManager().registerEvents(new GuiListener(rotationManager, provider), this);
        getServer().getPluginManager().registerEvents(new BroadcastListener(rotationManager, broadcastTask), this);

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

    public void reloadPluginConfig() {
        provider.updateConfig(this);
        this.settings = provider.getSettings();

        // Change to update managers instead
        penaltyManager.reloadSettings(settings);
        extractionManager.reloadSettings(settings);
        guiManager.reloadSettings(settings);
        rotationManager.reloadSettings(settings);
        weatherManager.reload(settings);

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

//    public BroadcastTask getBroadcastTask() {
//        return broadcastTask;
//    }
//
//    public DeathManager getDeathManager() {
//        return deathManager;
//    }
//
//    public RotationManager getRotationManager() {
//        return rotationManager;
//    }
//
//    public PenaltyManager getPenaltyManager() {
//        return penaltyManager;
//    }
//
//    public ExtractionManager getExtractionManager() {
//        return extractionManager;
//    }
//
//    public GuiManager getGuiManager() {
//        return guiManager;
//    }
//
//    public WeatherManager getWeatherManager() {
//        return weatherManager;
//    }
//
//    public AdventureSettings getSettings() {
//        return settings;
//    }
}
