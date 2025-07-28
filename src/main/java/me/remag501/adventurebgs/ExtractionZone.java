package me.remag501.adventurebgs;

import org.bukkit.Location;

public class ExtractionZone {
    private final String world;
    private final int minX, minY, minZ;
    private final int maxX, maxY, maxZ;

    public ExtractionZone(String world, int[] min, int[] max) {
        this.world = world;
        this.minX = Math.min(min[0], max[0]);
        this.minY = Math.min(min[1], max[1]);
        this.minZ = Math.min(min[2], max[2]);
        this.maxX = Math.max(min[0], max[0]);
        this.maxY = Math.max(min[1], max[1]);
        this.maxZ = Math.max(min[2], max[2]);
    }

    public boolean contains(Location loc) {
        if (!loc.getWorld().getName().equals(world)) return false;
        double x = loc.getX(), y = loc.getY(), z = loc.getZ();
        return x >= minX && x <= maxX &&
                y >= minY && y <= maxY &&
                z >= minZ && z <= maxZ;
    }
}

