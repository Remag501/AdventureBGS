package me.remag501.adventurebgs.model;

public class WorldInfo {
    private final String name;
    private final String texture;

    public WorldInfo(String name, String texture) {
        this.name = name;
        this.texture = texture;
    }

    public String getName() {
        return name;
    }

    public String getTexture() {
        return texture;
    }
}

