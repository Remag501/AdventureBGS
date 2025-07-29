package me.remag501.adventurebgs.managers;

import me.remag501.adventurebgs.AdventureBGS;
import me.remag501.adventurebgs.model.WorldInfo;
import me.remag501.adventurebgs.util.MessageUtil;
import me.remag501.adventurebgs.util.SkullUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GuiManager {

    private AdventureBGS plugin;

    public GuiManager (AdventureBGS plugin) {
        this.plugin = plugin;
    }

    public void openAdventureGUI(Player player) {
        String currentWorld = plugin.getRotationManager().getCurrentWorldName();
        String nextWorld = plugin.getRotationManager().getNextWorldName();
        long minutesLeft = plugin.getRotationManager().getMinutesUntilNextCycle();

        // GUI Title
        String guiTitle = MessageUtil.color(plugin.getConfig().getString("gui.title"));

        Inventory gui = Bukkit.createInventory(null, 36, guiTitle);

        // --- Teleport Item ---
        WorldInfo currentInfo = plugin.getRotationManager().getCurrentWorld();

        int tpSlot = plugin.getConfig().getInt("gui.teleport.slot", 13);
        String tpName = plugin.getConfig().getString("gui.teleport.name")
                .replace("%current_world%", currentWorld);

        List<String> tpLore = plugin.getConfig().getStringList("gui.teleport.lore")
                .stream()
                .map(line -> line.replace("%current_world%", currentWorld))
                .map(MessageUtil::color)
                .collect(Collectors.toList());

        String texture = currentInfo.getTexture();

        ItemStack teleportItem = SkullUtil.fromTexture(texture);
        ItemMeta tpMeta = teleportItem.getItemMeta();
        tpMeta.setDisplayName(MessageUtil.color(tpName));
        tpMeta.setLore(tpLore);
        teleportItem.setItemMeta(tpMeta);

        gui.setItem(tpSlot, teleportItem);

        // --- Info (Clock) Item ---
        int infoSlot = plugin.getConfig().getInt("gui.info.slot", 22);
        String infoName = plugin.getConfig().getString("gui.info.name");

        List<String> infoLore = plugin.getConfig().getStringList("gui.info.lore")
                .stream()
                .map(line -> line.replace("%minutes_left%", String.valueOf(minutesLeft)))
                .map(line -> line.replace("%next_world%", nextWorld))
                .map(MessageUtil::color)
                .collect(Collectors.toList());

        ItemStack clockItem = new ItemStack(Material.CLOCK);
        ItemMeta clockMeta = clockItem.getItemMeta();
        clockMeta.setDisplayName(MessageUtil.color(infoName));
        clockMeta.setLore(infoLore);
        clockItem.setItemMeta(clockMeta);

        gui.setItem(infoSlot, clockItem);

        player.openInventory(gui);
    }


}
