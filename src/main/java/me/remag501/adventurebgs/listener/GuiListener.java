package me.remag501.adventurebgs.listener;

import me.remag501.adventurebgs.AdventureBGS;
import me.remag501.adventurebgs.manager.PDCManager;
import me.remag501.adventurebgs.manager.RotationManager;
import me.remag501.adventurebgs.model.RotationTrack;
import me.remag501.adventurebgs.setting.SettingsProvider;
import me.remag501.adventurebgs.util.MessageUtil;
import me.remag501.bgscore.api.event.EventService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.persistence.PersistentDataType;

public class GuiListener {

    private final RotationManager rotationManager;
    private final PDCManager pdcManager;
    private final NamespacedKey worldIdKey;

    public GuiListener(EventService eventService, AdventureBGS plugin, PDCManager pdcManager, RotationManager rotationManager, SettingsProvider provider) {
        this.pdcManager = pdcManager;
        this.rotationManager = rotationManager;
        // Pre-cache the key so we don't recreate it every click
        this.worldIdKey = new NamespacedKey(plugin, "world_id");

        eventService.subscribe(InventoryClickEvent.class)
                // Filter: Only handle clicks in our specific GUI
                .filter(event -> {
                    String guiTitle = MessageUtil.color(provider.getSettings().getGuiTitle());
                    return event.getView().getTitle().equals(guiTitle);
                })
                .handler(this::handleGuiClick);
    }

    private void handleGuiClick(InventoryClickEvent event) {
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player) || event.getCurrentItem() == null) return;

        if (event.getCurrentItem().getType() == Material.PLAYER_HEAD) {
            String actionId = event.getCurrentItem().getItemMeta()
                    .getPersistentDataContainer()
                    .get(worldIdKey, PersistentDataType.STRING);

            if (actionId == null) return;

            for (RotationTrack rotationTrack : rotationManager.getTracks()) {
                if (actionId.equals(rotationTrack.getId())) {
                    String currentWorld = rotationTrack.getCurrentWorld().getId();

                    // Teleport and Sync
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "rtp player " + player.getName() + " " + currentWorld);
                    player.closeInventory();

                    // Ensure the world exists before syncing
                    org.bukkit.World targetWorld = Bukkit.getWorld(currentWorld);
                    if (targetWorld != null) {
                        pdcManager.syncPlayerToWorld(player, targetWorld);
                    }
                    break;
                }
            }
        }
    }
}