package me.remag501.adventure.model;

import java.util.ArrayList;
import java.util.List;

public class WorldInfo {
    private final String id;
    private final String texture;
    private String chatName;
    private String guiName;

    private final List<String> lore;
    private final List<String> commands;

    public WorldInfo(String id, String texture, String chatName, String guiName, List<String> lore, List<String> commands) {
        this.id = id;
        this.texture = texture;
        this.chatName = chatName;
        this.guiName = guiName;
        this.lore = lore != null ? lore : new ArrayList<>();
        this.commands = commands != null ? commands : new ArrayList<>();

    }

    public String getId() {
        return id;
    }

    public String getTexture() {
        return texture;
    }

    public String getChatName() {
        return chatName;
    }

    public String getGuiName() {
        return guiName;
    }

    public List<String> getLore() {
        return lore;
    }

    public List<String> getCommands() {
        return commands;
    }

}

