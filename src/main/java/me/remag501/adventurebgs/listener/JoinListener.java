package me.remag501.adventurebgs.listener;

import me.remag501.adventurebgs.manager.PDCManager;
import me.remag501.adventurebgs.manager.PenaltyManager;
import me.remag501.adventurebgs.manager.RotationManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    private final PDCManager pdcManager;
    private final PenaltyManager penaltyManager;
    private final RotationManager rotationManager;

    public JoinListener(PDCManager pdcManager, RotationManager rotationManager, PenaltyManager penaltyManager) {
        this.pdcManager = pdcManager;
        this.rotationManager = rotationManager;
        this.penaltyManager = penaltyManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();

        // Check if this is even an adventure world
        if (rotationManager.getTrackByWorld(world) == null) return;

        // Use the manager to check the versions
        if (pdcManager.isPlayerOutdated(player, world)) {

            int pVer = pdcManager.getPlayerVersionForWorld(player, world);
            int wVer = pdcManager.getWorldVersion(world);
            Bukkit.getLogger().info("[Adventure] Penalizing " + player.getName() + ": Player Ver " + pVer + " < World Ver " + wVer);

            player.sendMessage("§c§l(!) §cYou left the game before extracting!");
            penaltyManager.penalizePlayer(player);

            // Sync them so they are safe until the next reset
            pdcManager.syncPlayerToWorld(player, world);
        }
    }
}