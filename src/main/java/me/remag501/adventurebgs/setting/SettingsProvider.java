package me.remag501.adventurebgs.setting;

import me.remag501.adventurebgs.AdventureBGS;

public class SettingsProvider {

    private AdventureSettings settings;

    public SettingsProvider(AdventureBGS plugin) {
        updateConfig(plugin);
    }

    public void updateConfig(AdventureBGS plugin) {
        settings = new AdventureSettings(plugin.getConfig());
    }

    public AdventureSettings getSettings() {
        return settings;
    }

}
