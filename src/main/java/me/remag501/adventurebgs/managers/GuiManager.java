package me.remag501.adventurebgs.managers;

import me.remag501.adventurebgs.AdventureBGS;
import me.remag501.adventurebgs.AdventureSettings;
import me.remag501.adventurebgs.model.RotationTrack;
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

    private AdventureSettings settings;

    private final RotationManager rotationManager;

    public GuiManager (RotationManager manager, AdventureSettings settings) {
        this.rotationManager = manager;
        this.settings = settings;
    }

    public void openAdventureGUI(Player player) {
        RotationTrack rotationTrack = rotationManager.getTrackByWorld(Bukkit.getWorld("Sahara")); // Hardcoded temporarily, will be replaced with for loop

        String currentWorld = rotationTrack.getCurrentWorld().getGuiName();
        String nextWorld = rotationTrack.getNextWorld().getGuiName();
        long minutesLeft = rotationTrack.getMinutesUntilNextCycle();

        // GUI Title
        String guiTitle = MessageUtil.color(settings.getGuiTitle());

        Inventory gui = Bukkit.createInventory(null, 36, guiTitle);

        // --- Teleport Item ---
        WorldInfo currentInfo = rotationTrack.getCurrentWorld();

        int tpSlot = settings.getGuiTeleportSlot();
        String tpName = settings.getGuiTeleportName()
                .replace("%current_world%", currentWorld);

        List<String> tpLore = rotationTrack.getCurrentWorld().getLore()
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
        int infoSlot = settings.getGuiInfoSlot();
        String infoName = settings.getGuiInfoName();

        List<String> infoLore = settings.getGuiInfoLore()
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

    public void reloadSettings(AdventureSettings settings) {
        this.settings = settings;
    }


}
