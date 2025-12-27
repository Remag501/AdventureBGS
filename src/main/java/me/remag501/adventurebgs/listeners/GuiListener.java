package me.remag501.adventurebgs.listeners;

import me.remag501.adventurebgs.AdventureBGS;
import me.remag501.adventurebgs.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.Plugin;

public class GuiListener implements Listener {

    private AdventureBGS plugin;

    public GuiListener(AdventureBGS plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String guiTitle = MessageUtil.color(plugin.getConfig().getString("gui.title"));
        String viewTitle = event.getView().getTitle();

        if (!viewTitle.equals(guiTitle)) return;

        event.setCancelled(true); // Prevent taking items

        if (event.getCurrentItem() == null) return;

        AdventureBGS plugin = (AdventureBGS) Bukkit.getPluginManager().getPlugin("AdventureBGS");

        if (event.getCurrentItem().getType() == Material.PLAYER_HEAD) {
            // Teleport with BetterRTP
            String currentWorld = plugin.getRotationManager().getCurrentWorldName();
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "rtp player " + player.getName() + " " + currentWorld);
            player.closeInventory();
        }
    }


}
