package me.remag501.adventurebgs;

import me.remag501.adventurebgs.commands.AdventureCommand;
import me.remag501.adventurebgs.managers.RotationManager;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.Bukkit;
import org.bukkit.World;

public class AdventurePlugin extends JavaPlugin {

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
}
