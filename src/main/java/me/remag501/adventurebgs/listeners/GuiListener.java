package me.remag501.adventurebgs.listeners;

import me.remag501.adventurebgs.AdventureBGS;
import me.remag501.adventurebgs.setting.SettingsProvider;
import me.remag501.adventurebgs.managers.RotationManager;
import me.remag501.adventurebgs.model.RotationTrack;
import me.remag501.adventurebgs.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.persistence.PersistentDataType;

public class GuiListener implements Listener {

    private final RotationManager rotationManager;
    private final SettingsProvider provider;
    private final AdventureBGS plugin;

    public GuiListener(AdventureBGS plugin, RotationManager rotationManager, SettingsProvider provider) {
        this.plugin = plugin;
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

            NamespacedKey guiKey = new NamespacedKey(plugin, "world_id");
            String actionId = event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(guiKey, PersistentDataType.STRING);

            for (RotationTrack rotationTrack: rotationManager.getTracks()) {

                if (actionId.equals(rotationTrack.getId())) {
                    // Teleport with BetterRTP
                    String currentWorld = rotationTrack.getCurrentWorld().getId();
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "rtp player " + player.getName() + " " + currentWorld);
                    player.closeInventory();
                    break;
                }
            }
        }
    }


}
