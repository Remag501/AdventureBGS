package me.remag501.adventurebgs.listeners;

import me.remag501.adventurebgs.AdventureBGS;
import me.remag501.adventurebgs.SettingsProvider;
import me.remag501.adventurebgs.managers.RotationManager;
import me.remag501.adventurebgs.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.Plugin;

public class GuiListener implements Listener {

    private final RotationManager rotationManager;
    private final SettingsProvider provider;

    public GuiListener(RotationManager rotationManager, SettingsProvider provider) {
        this.rotationManager = rotationManager;
        this.provider = provider;
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String guiTitle = MessageUtil.color(provider.getSettings().getGuiTitle());
        String viewTitle = event.getView().getTitle();

        if (!viewTitle.equals(guiTitle)) return;

        event.setCancelled(true); // Prevent taking items

        if (event.getCurrentItem() == null) return;

        if (event.getCurrentItem().getType() == Material.PLAYER_HEAD) {
            // Teleport with BetterRTP
            String currentWorld = rotationManager.getCurrentWorldName();
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "rtp player " + player.getName() + " " + currentWorld);
            player.closeInventory();
        }
    }


}
