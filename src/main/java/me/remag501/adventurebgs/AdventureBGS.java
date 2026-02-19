package me.remag501.adventurebgs;

import me.remag501.adventurebgs.command.AdventureCommand;
import me.remag501.adventurebgs.listener.BroadcastListener;
import me.remag501.adventurebgs.listener.ExtractionListener;
import me.remag501.adventurebgs.listener.GuiListener;
import me.remag501.adventurebgs.listener.JoinListener;
import me.remag501.adventurebgs.manager.*;
import me.remag501.adventurebgs.placeholder.BGSExpansion;
import me.remag501.adventurebgs.setting.AdventureSettings;
import me.remag501.adventurebgs.setting.SettingsProvider;
import me.remag501.adventurebgs.task.BroadcastTask;
import me.remag501.bgscore.api.BGSApi;
import me.remag501.bgscore.api.command.CommandService;
import me.remag501.bgscore.api.event.EventService;
import me.remag501.bgscore.api.namespace.NamespaceService;
import me.remag501.bgscore.api.task.TaskService;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.Bukkit;

import java.util.UUID;

public class AdventureBGS extends JavaPlugin {

    public static final UUID SYSTEM_ID = UUID.nameUUIDFromBytes("adventure-system".getBytes());

    private RotationManager rotationManager;
    private GuiManager guiManager;
    private ExtractionManager extractionManager;
    private PenaltyManager penaltyManager;
    private WeatherManager weatherManager;
    private BroadcastTask broadcastTask;
    private PDCManager pdcManager;

    private AdventureSettings settings;
    private SettingsProvider provider;


    @Override
    public void onEnable() {
        // Config stuff
        saveDefaultConfig();
        this.provider = new SettingsProvider(this);
        this.settings = provider.getSettings();

        // Get services from BGS Api
        EventService eventService = BGSApi.events();
        TaskService taskService = BGSApi.tasks();
        CommandService commandService = BGSApi.commands();
        NamespaceService namespaceService = BGSApi.namespaces();

        // Initialize managers
        // Independent Managers (Leaves)
        this.extractionManager = new ExtractionManager(settings);
        this.pdcManager = new PDCManager(this);
        this.rotationManager = new RotationManager(taskService, settings);

        // Managers that need Rotation (Workers)
        this.broadcastTask = new BroadcastTask(taskService, rotationManager, settings);
        this.weatherManager = new WeatherManager(taskService, settings);

        // Complex Managers (Controllers)
        this.penaltyManager = new PenaltyManager(taskService, pdcManager, broadcastTask, settings);
        this.guiManager = new GuiManager(this, rotationManager, settings);

        // Register commands
        getCommand("adventure").setExecutor(new AdventureCommand(this, pdcManager, rotationManager, guiManager));

        // Register Listener
//        getServer().getPluginManager().registerEvents(, this);
        new ExtractionListener(eventService, taskService, extractionManager, rotationManager, provider);
//        getServer().getPluginManager().registerEvents(, this);
        new JoinListener(eventService, pdcManager, rotationManager, penaltyManager);
//        getServer().getPluginManager().registerEvents(, this);
        new GuiListener(eventService, this, pdcManager, rotationManager, provider);
//        getServer().getPluginManager().registerEvents(, this);
        new BroadcastListener(eventService, rotationManager);

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

}
