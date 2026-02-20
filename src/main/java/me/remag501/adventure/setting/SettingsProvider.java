package me.remag501.adventure.setting;

import me.remag501.adventure.AdventurePlugin;

public class SettingsProvider {

    private AdventureSettings settings;

    public SettingsProvider(AdventurePlugin plugin) {
        updateConfig(plugin);
    }

    public void updateConfig(AdventurePlugin plugin) {
        settings = new AdventureSettings(plugin.getConfig());
    }

    public AdventureSettings getSettings() {
        return settings;
    }

}
