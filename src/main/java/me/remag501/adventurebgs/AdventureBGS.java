package me.remag501.adventurebgs;

import me.remag501.adventurebgs.commands.AdventureCommand;
import me.remag501.adventurebgs.listeners.BroadcastListener;
import me.remag501.adventurebgs.listeners.ExtractionListener;
import me.remag501.adventurebgs.listeners.GuiListener;
import me.remag501.adventurebgs.listeners.JoinListener;
import me.remag501.adventurebgs.managers.*;
import me.remag501.adventurebgs.placeholder.BGSExpansion;
import me.remag501.adventurebgs.setting.AdventureSettings;
import me.remag501.adventurebgs.setting.SettingsProvider;
import me.remag501.adventurebgs.tasks.BroadcastTask;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.Bukkit;

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
        // 1. Independent Managers (Leaves)
        this.extractionManager = new ExtractionManager(settings);
        this.deathManager = new DeathManager(this);
        this.rotationManager = new RotationManager(this, settings);

        // 2. Managers that need Rotation (Workers)
        this.broadcastTask = new BroadcastTask(this, rotationManager, settings);
        this.weatherManager = new WeatherManager(this, settings);

        // 3. Complex Managers (Controllers)
        this.penaltyManager = new PenaltyManager(this, rotationManager, broadcastTask, settings);
        this.guiManager = new GuiManager(this, rotationManager, settings);

        // Preload all configured worlds
//        preloadWorlds();

        // Register commands
        getCommand("adventure").setExecutor(new AdventureCommand(this, rotationManager, guiManager));

        // Register Listener
        getServer().getPluginManager().registerEvents(new ExtractionListener(this, extractionManager, rotationManager, provider), this);
        getServer().getPluginManager().registerEvents(new JoinListener(this, penaltyManager), this);
        getServer().getPluginManager().registerEvents(new GuiListener(this, rotationManager, provider), this);
        getServer().getPluginManager().registerEvents(new BroadcastListener(rotationManager, broadcastTask), this);

        // Start broadcasting messages
        penaltyManager.startBroadcastTask();

        // Register placeholder
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new BGSExpansion(rotationManager).register();
            getLogger().info("AdventureBGS Placeholders registered!");
        }
    }

    @Override
    public void onDisable() {
        // Cleanup if needed
    }

    public void reloadPluginConfig() {
        this.reloadConfig();
        provider.updateConfig(this);
        this.settings = provider.getSettings();

        // Change to update managers instead
        penaltyManager.reloadSettings(settings);
        extractionManager.reloadSettings(settings);
        guiManager.reloadSettings(settings);
        rotationManager.reloadSettings(settings);
        weatherManager.reload(settings);

    }

//    private void preloadWorlds() {
//        for (String worldName : rotationManager.getWorlds()) {
//            World world = Bukkit.getWorld(worldName);
//            if (world == null) {
//                getLogger().warning("World not loaded: " + worldName);
//            } else {
//                getLogger().info("Preloaded world: " + worldName);
//            }
//        }
//    }
}
