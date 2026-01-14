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
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GuiManager {

    private AdventureSettings settings;

    private final RotationManager rotationManager;
    private final AdventureBGS plugin;

    public GuiManager (AdventureBGS plugin, RotationManager manager, AdventureSettings settings) {
        this.plugin = plugin;
        this.rotationManager = manager;
        this.settings = settings;
    }

    public void openAdventureGUI(Player player) {

        // GUI Title
        String guiTitle = MessageUtil.color(settings.getGuiTitle());
        Inventory gui = Bukkit.createInventory(null, 36, guiTitle);

        for (RotationTrack rotationTrack: rotationManager.getTracks()) {
            String currentWorld = rotationTrack.getCurrentWorld().getGuiName();
            String nextWorld = rotationTrack.getNextWorld().getGuiName();
            long minutesLeft = rotationTrack.getMinutesUntilNextCycle();

            // --- Teleport Item ---
            WorldInfo currentInfo = rotationTrack.getCurrentWorld();

            int tpSlot = rotationTrack.getGui().getTeleportSlot();
            String tpName = rotationTrack.getGui().getTeleportName()
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
            // Add persistent data container for checking on listener
            String id = rotationTrack.getId();
            NamespacedKey guiKey = new NamespacedKey(plugin, "world_id");
            tpMeta.getPersistentDataContainer().set(guiKey, PersistentDataType.STRING, id);

            teleportItem.setItemMeta(tpMeta);

            gui.setItem(tpSlot, teleportItem);

            // --- Info (Clock) Item ---
            int infoSlot = rotationTrack.getGui().getInfoSlot();
            String infoName = rotationTrack.getGui().getInfoName();

            List<String> infoLore = rotationTrack.getGui().getInfoLore()
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

        }

        player.openInventory(gui);

    }

    public void reloadSettings(AdventureSettings settings) {
        this.settings = settings;
    }


}
