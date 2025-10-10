package me.remag501.adventurebgs.model;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.List;

public class ExtractionZone {

    private final String world;
    private final int minX, minY, minZ;
    private final int maxX, maxY, maxZ;

    private final int portalMinX, portalMinY, portalMinZ;
    private final int portalMaxX, portalMaxY, portalMaxZ;

    private final Location beaconLoc;
    private final Location particleLoc;

    public ExtractionZone(String world, int[] min, int[] max, int[] portalMin, int[] portalMax,
                          List<Double> beaconLoc, List<Double> particleLoc) {

        this.world = world;

        // Main zone
        this.minX = Math.min(min[0], max[0]);
        this.minY = Math.min(min[1], max[1]);
        this.minZ = Math.min(min[2], max[2]);
        this.maxX = Math.max(min[0], max[0]);
        this.maxY = Math.max(min[1], max[1]);
        this.maxZ = Math.max(min[2], max[2]);

        // Portal bounds
        this.portalMinX = Math.min(portalMin[0], portalMax[0]);
        this.portalMinY = Math.min(portalMin[1], portalMax[1]);
        this.portalMinZ = Math.min(portalMin[2], portalMax[2]);
        this.portalMaxX = Math.max(portalMin[0], portalMax[0]);
        this.portalMaxY = Math.max(portalMin[1], portalMax[1]);
        this.portalMaxZ = Math.max(portalMin[2], portalMax[2]);

        // Beacon location
        this.beaconLoc = beaconLoc != null && beaconLoc.size() == 3
                ? new Location(Bukkit.getWorld(world), beaconLoc.get(0), beaconLoc.get(1), beaconLoc.get(2))
                : null;

        // Particle location
        this.particleLoc = particleLoc != null && particleLoc.size() == 3
                ? new Location(Bukkit.getWorld(world), particleLoc.get(0), particleLoc.get(1), particleLoc.get(2))
                : null;
    }

    public boolean contains(Location loc) {
        if (!loc.getWorld().getName().equals(world)) return false;
        double x = loc.getX(), y = loc.getY(), z = loc.getZ();
        return x >= minX && x <= maxX &&
                y >= minY && y <= maxY &&
                z >= minZ && z <= maxZ;
    }

    public boolean inPortal(Location loc) {
        if (!loc.getWorld().getName().equals(world)) return false;
        double x = loc.getX(), y = loc.getY(), z = loc.getZ();
        return x >= portalMinX && x <= portalMaxX &&
                y >= portalMinY && y <= portalMaxY &&
                z >= portalMinZ && z <= portalMaxZ;
    }

    public Location getBeaconLoc() {
        return beaconLoc;
    }

    public Location getParticleLoc() {
        return particleLoc;
    }

    public String getWorld() {
        return world;
    }
}
