package me.remag501.adventure.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public class BeaconColorUtil {

    /**
     * Changes the color of a beacon's beam by clearing any blocks above it
     * and placing a stack of stained glass blocks. The resulting color is
     * a mix of all the provided colors.
     *
     * @param beaconLocation The location of the beacon block.
     * @param colors The Material(s) of the stained glass blocks to place.
     * You can provide a single color or multiple for stacking.
     */
    public static void setBeaconBeamColors(Location beaconLocation, Material... colors) {
        if (beaconLocation == null || colors.length == 0) {
            return;
        }

        World world = beaconLocation.getWorld();
        if (world == null) {
            return;
        }

        // Start one block above the beacon.
        Location currentLocation = beaconLocation.clone().add(0, 1, 0);

        // First, clear the blocks above the beacon to ensure a clean path.
        // We go all the way up to the world height limit.
        for (int i = 0; i < 256 - beaconLocation.getY(); i++) {
            Block blockToClear = world.getBlockAt(currentLocation.clone().add(0, i, 0));
            if (blockToClear.getType() != Material.AIR) {
                blockToClear.setType(Material.AIR);
            }
        }

        // Now, place the new stack of stained glass blocks one on top of the other.
        Location placeLoc = beaconLocation.clone().add(0, 1, 0);
        for (Material material : colors) {
            if (material.name().contains("_STAINED_GLASS")) {
                world.getBlockAt(placeLoc).setType(material);
                placeLoc.add(0, 1, 0); // Move one block up for the next glass block.
            }
        }
    }
}
