package me.remag501.adventurebgs.model;

import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.function.Function;

public final class TrackGuiConfig {

    private final int teleportSlot;
    private final String teleportName;

    private final int infoSlot;
    private final String infoName;
    private final List<String> infoLore;

    public TrackGuiConfig(ConfigurationSection sec, Function<String, String> color, Function<List<String>, List<String>> colorList) {
        this.teleportSlot = sec.getInt("teleport.slot");
        this.teleportName = color.apply(sec.getString("teleport.name"));

        this.infoSlot = sec.getInt("info.slot");
        this.infoName = color.apply(sec.getString("info.name"));
        this.infoLore = colorList.apply(sec.getStringList("info.lore"));
    }

    public int getTeleportSlot() {
        return teleportSlot;
    }

    public String getTeleportName() {
        return teleportName;
    }

    public int getInfoSlot() {
        return infoSlot;
    }

    public String getInfoName() {
        return infoName;
    }

    public List<String> getInfoLore() {
        return infoLore;
    }

}
