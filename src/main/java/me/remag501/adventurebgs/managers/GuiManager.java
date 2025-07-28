package me.remag501.adventurebgs.managers;

import me.remag501.adventurebgs.AdventureBGS;
import me.remag501.adventurebgs.model.WorldInfo;
import me.remag501.adventurebgs.util.SkullUtil;
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

        String guiTitle = plugin.getConfig().getString("gui.title");
        Inventory gui = Bukkit.createInventory(null, 36, guiTitle);

        // Teleport block
        WorldInfo currentInfo = plugin.getRotationManager().getCurrentWorld();
        ItemStack teleportItem = SkullUtil.fromTexture(currentInfo.getTexture());
        ItemMeta tpMeta = teleportItem.getItemMeta();
        tpMeta.setDisplayName(ChatColor.GREEN + "Enter: " + currentInfo.getName());
        teleportItem.setItemMeta(tpMeta);
        gui.setItem(13, teleportItem);

        // Clock with lore
        ItemStack clockItem = new ItemStack(Material.CLOCK);
        ItemMeta clockMeta = clockItem.getItemMeta();
        clockMeta.setDisplayName(ChatColor.YELLOW + "Cycle Info");
        clockMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Closes in: " + minutesLeft + " minutes",
                ChatColor.GRAY + "Next map: " + nextWorld
        ));
        clockItem.setItemMeta(clockMeta);
        gui.setItem(22, clockItem);

        player.openInventory(gui);
    }

}
