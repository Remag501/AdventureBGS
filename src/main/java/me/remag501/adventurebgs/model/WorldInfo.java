package me.remag501.adventurebgs.model;

public class WorldInfo {
    private final String id;
    private final String texture;
    private String chatName;
    private String guiName;

    public WorldInfo(String id, String texture, String chatName, String guiName) {
        this.id = id;
        this.texture = texture;
        this.chatName = chatName;
        this.guiName = guiName;

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
}

