package me.remag501.adventurebgs.managers;

import me.remag501.adventurebgs.AdventureBGS;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class GuiManager {

    private AdventureBGS plugin;

    public GuiManager (AdventureBGS plugin) {
        this.plugin = plugin;
    }

    public void openAdventureGUI(Player player) {
        String currentWorld = plugin.getRotationManager().getCurrentWorldName();
        String nextWorld = plugin.getRotationManager().getNextWorldName();

        // Time remaining until next rotation
        long minutesLeft = plugin.getRotationManager().getMinutesUntilNextCycle();

        Inventory gui = Bukkit.createInventory(null, 9, "Adventure Worlds");

        // Teleport block
        ItemStack teleportItem = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta tpMeta = teleportItem.getItemMeta();
        tpMeta.setDisplayName(ChatColor.GREEN + "Enter: " + currentWorld);
        teleportItem.setItemMeta(tpMeta);
        gui.setItem(4, teleportItem);

        // Clock with lore
        ItemStack clockItem = new ItemStack(Material.CLOCK);
        ItemMeta clockMeta = clockItem.getItemMeta();
        clockMeta.setDisplayName(ChatColor.YELLOW + "Cycle Info");
        clockMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Closes in: " + minutesLeft + " minutes",
                ChatColor.GRAY + "Next map: " + nextWorld
        ));
        clockItem.setItemMeta(clockMeta);
        gui.setItem(5, clockItem);

        player.openInventory(gui);
    }

}
