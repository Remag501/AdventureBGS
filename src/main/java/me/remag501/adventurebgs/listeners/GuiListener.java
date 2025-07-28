package me.remag501.adventurebgs.listeners;

import me.remag501.adventurebgs.AdventureBGS;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.Plugin;

public class GuiListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        if (!event.getView().getTitle().equals("Adventure Worlds")) return;

        event.setCancelled(true); // Prevent taking items

        if (event.getCurrentItem() == null) return;

        AdventureBGS plugin = (AdventureBGS) Bukkit.getPluginManager().getPlugin("AdventureBGS");

        if (event.getSlot() == 4) {
            // Teleport with BetterRTP
            String currentWorld = plugin.getRotationManager().getCurrentWorldName();
            Bukkit.dispatchCommand(player, "rtp world " + currentWorld);
            player.closeInventory();
        }
    }


}
