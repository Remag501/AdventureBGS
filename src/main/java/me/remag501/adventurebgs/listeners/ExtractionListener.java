package me.remag501.adventurebgs.listeners;

import me.remag501.adventurebgs.AdventureBGS;
import me.remag501.adventurebgs.managers.ExtractionManager;
import me.remag501.adventurebgs.model.ExtractionZone;
import me.remag501.adventurebgs.util.MessageUtil;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ExtractionListener implements Listener {

    private final AdventureBGS plugin;
    private final ExtractionManager manager;
    private final int extractionDuration;

    public ExtractionListener(AdventureBGS plugin) {
        this.plugin = plugin;
        this.manager = plugin.getExtractionManager();
        this.extractionDuration = plugin.getConfig().getInt("extraction.duration");
    }

    /**
     * Sends a message to all online players currently within the bounds of the given ExtractionZone.
     */
    private void messagePlayersInZone(ExtractionZone zone, String message) {
        World world = Bukkit.getWorld(zone.getWorld());
        if (world == null) return;
        String colorMessage = MessageUtil.color(message);

        for (Player p : world.getPlayers()) {
            // Only send message if the player is actually standing in the zone
            if (zone.contains(p.getLocation())) {
                p.sendMessage(colorMessage);
            }
        }
    }

    /**
     * Helper to find the correct active BossBar (Extraction, Portal, or Cooldown)
     * for a zone and add the player to it, ensuring synchronization.
     */
    private void syncPlayerBossBar(Player player, ExtractionZone zone) {
        BossBar activeBar = null;

        // 1. Check Extraction Timer
        if (zone.isExtracting() && zone.getExtractionBossBar() != null) {
            activeBar = zone.getExtractionBossBar();
        }
        // 2. Check Portal Open Phase
        else if (zone.isPortalOpen() && zone.getPortalBossBar() != null) {
            activeBar = zone.getPortalBossBar();
        }
        // 3. Check Cooldown Phase
        else if (zone.isDown() && zone.getCooldownBossBar() != null) {
            activeBar = zone.getCooldownBossBar();
        }

        if (activeBar != null && !activeBar.getPlayers().contains(player)) {
            activeBar.addPlayer(player);
        }
    }

    // Removed onPlayerJoin handler as players do not spawn/relog into an extraction zone.

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // 1. Ignore micro movements
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        // 2. Ignore players not in extraction world
        if (!player.getWorld().getName().equals(plugin.getRotationManager().getCurrentWorld().getId()))
            return;

        // 3. Find the zone the player is moving into and the zone they are moving from
        ExtractionZone fromZone = manager.getZone(event.getFrom());
        ExtractionZone toZone = manager.getZone(event.getTo());

        // --- PART A: Handle Player Status (Adding/Removing from Bars) ---

        // A1. Player is leaving a zone (Handle Extraction, Portal, & Cooldown Bars)
        if (fromZone != null && fromZone != toZone) {
            // If the zone was extracting, remove the player and check for cancellation
            if (fromZone.isExtracting()) {
                // Remove player from the extraction BossBar when they leave the zone
                if (fromZone.getExtractionBossBar() != null) {
                    fromZone.getExtractionBossBar().removePlayer(player);
                }

                fromZone.removeExtractingPlayer(player);

                // If the zone is now empty, cancel the entire extraction (Zone-centric cancellation)
                if (fromZone.isEmpty()) {
                    fromZone.cancelExtraction();

                    // Cancellation message: RESERVED for players inside the zone
                    String cancelMessage = MessageUtil.color(plugin.getConfig().getString("extraction.message.cancel"));

                    // Only message players who are in the now-cancelled zone
                    messagePlayersInZone(fromZone, cancelMessage);
                }
            }
            // If the portal is open, remove them from the Portal BossBar
            else if (fromZone.isPortalOpen() && fromZone.getPortalBossBar() != null) {
                fromZone.getPortalBossBar().removePlayer(player);
            }
            // If the zone was down, remove them from the Cooldown BossBar (they left the down area)
            if (fromZone.isDown() && fromZone.getCooldownBossBar() != null) {
                fromZone.getCooldownBossBar().removePlayer(player);
            }
        }

        // A2. Player is entering a zone (Handle Extraction, Portal, & Cooldown Bars)
        if (toZone != null && fromZone != toZone) {
            // If the zone is currently running an extraction timer, add them to the list of extractors
            if (toZone.isExtracting()) {
                toZone.addExtractingPlayer(player);
            }

            // CONSOLIDATED FIX: Add the player to the correct active BossBar.
            syncPlayerBossBar(player, toZone);
        }


        // --- PART B: Handle Extraction Start Logic ---

        // 4. Check if the player is in an eligible zone to START a new extraction
        if (toZone != null && toZone.isEnabled() && !toZone.isExtracting()) {
            startExtraction(player, toZone);
        }
    }

    private void startExtraction(Player player, ExtractionZone zone) {
        // Broadcast start message: RESERVED for players inside the zone
        String rawStartMsg = plugin.getConfig().getString("extraction.message.start");
        String zoneNamePlaceholder = "Extraction Zone";
        String startMsg = MessageUtil.color(rawStartMsg
                .replace("%seconds%", String.valueOf(extractionDuration))
                .replace("%zone%", zoneNamePlaceholder));

        // Message restricted to players currently in the zone
        messagePlayersInZone(zone, startMsg);

        String title = MessageUtil.color(plugin.getConfig().getString("extraction.message.title"));

        BossBar bossBar = Bukkit.createBossBar(
                title,
                BarColor.GREEN,
                BarStyle.SOLID
        );

        // FIX: Explicitly add the player who triggered the extraction to the BossBar immediately.
        bossBar.addPlayer(player);

        // Add ONLY players currently inside the zone to the BossBar (Local visibility)
        World world = player.getWorld();
        for (Player p : world.getPlayers()) {
            if (zone.contains(p.getLocation())) {
                // The addPlayer method is idempotent, so calling it again for the trigger player is safe.
                bossBar.addPlayer(p);
            }
        }

        zone.startExtraction(null, bossBar, player);

        BukkitRunnable task = new BukkitRunnable() {
            int timeLeft = extractionDuration;
            boolean alertTriggered = false;

            @Override
            public void run() {
                if (!zone.isExtracting() || zone.getExtractionBossBar() == null) {
                    cancel();
                    return;
                }

                if (timeLeft <= 0) {
                    if (!zone.isEmpty()) {
                        completeExtraction(zone);
                    } else {
                        zone.cancelExtraction();
                    }
                    cancel();
                    return;
                }

                int alertSeconds = plugin.getConfig().getInt("extraction.alert-seconds", 3);
                if (timeLeft == alertSeconds && !alertTriggered) {
                    // TODO: Implement zone-based alert here
                    // triggerAlert(zone);
                    alertTriggered = true;
                }

                zone.getExtractionBossBar().setProgress((double) timeLeft / extractionDuration);
                String updatedTitle = title.replace("%seconds%", String.valueOf(timeLeft));
                zone.getExtractionBossBar().setTitle(updatedTitle);
                timeLeft--;
            }
        };

        zone.setExtractionTask(task);
        task.runTaskTimer(plugin, 0L, 20L);
    }

    // Refactored to operate on the ZONE
    private void completeExtraction(ExtractionZone zone) {
        World world = Bukkit.getWorld(zone.getWorld());
        if (world == null) return;

        // 1. Snapshot the players who successfully completed the extraction
        Set<UUID> successfulPlayers = new HashSet<>(zone.getExtractingPlayers());

        // 2. Clean up the extraction state (This also clears the extractingPlayers list and removes the BossBar from all viewers)
        zone.cancelExtraction();

        String successMsg = MessageUtil.color(plugin.getConfig().getString("extraction.message.success"));

        // SUCCESS MESSAGE: ONLY to the players who successfully extracted
        for (UUID uuid : successfulPlayers) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                p.sendMessage(successMsg);
            }
        }

        // --- Phase 1: Open the portal (replace gate with air) ---
        List<Location> gateBlocks = zone.getPortalGateBlocks();
        Map<Location, Material> originalBlocks = new HashMap<>();
        for (Location loc : gateBlocks) {
            originalBlocks.put(loc, loc.getBlock().getType());
            loc.getBlock().setType(Material.AIR);
        }

        zone.setEnabled(false); // Zone is now disabled for the cooldown
        zone.setPortalOpen(true);

        // --- Portal open phase ---
        int portalOpenSeconds = plugin.getConfig().getInt("extraction.portal-open-seconds", 7);
        String portalOpenMessage = plugin.getConfig().getString("extraction.message.portal-open")
                .replace("%seconds%", String.valueOf(portalOpenSeconds));

        // PORTAL OPEN MESSAGE: RESERVED for players *currently* inside the zone (excludes successful extractors)
        messagePlayersInZone(zone, portalOpenMessage);

        BossBar portalBar = Bukkit.createBossBar(
                ChatColor.AQUA + "Portal open for " + portalOpenSeconds + "s",
                BarColor.BLUE,
                BarStyle.SOLID
        );

        // Store the portal bar in the zone model
        zone.setPortalBossBar(portalBar);

        // BossBar is local for players currently inside the zone
        for (Player p : world.getPlayers()) {
            if (zone.contains(p.getLocation())) {
                portalBar.addPlayer(p);
            }
        }

        BukkitRunnable portalTask = new BukkitRunnable() {
            int timeLeft = portalOpenSeconds;

            @Override
            public void run() {
                if (timeLeft <= 0) {
                    portalBar.removeAll();
                    cancel();
                    zone.setPortalOpen(false); // This setter now cleans up the portalBossBar in the model

                    // --- Phase 2: Close portal (restore blocks) ---
                    for (Map.Entry<Location, Material> entry : originalBlocks.entrySet()) {
                        entry.getKey().getBlock().setType(entry.getValue());
                    }

                    // --- Phase 3: Extraction down (cooldown) ---
                    int zoneDownSeconds = plugin.getConfig().getInt("extraction.down-seconds", 10);
                    String zoneDownMsg = plugin.getConfig().getString("extraction.message.down")
                            .replace("%seconds%", String.valueOf(zoneDownSeconds));

                    // ZONE DOWN MESSAGE: RESERVED for players inside the zone
                    messagePlayersInZone(zone, zoneDownMsg);

                    BossBar downBar = Bukkit.createBossBar(
                            ChatColor.RED + "Extraction down for " + zoneDownSeconds + "s",
                            BarColor.RED,
                            BarStyle.SOLID
                    );

                    // BossBar is local for players currently inside the zone
                    for (Player p : world.getPlayers()) {
                        if (zone.contains(p.getLocation())) {
                            downBar.addPlayer(p);
                        }
                    }
                    zone.setCooldownState(downBar);

                    new BukkitRunnable() {
                        int downTimeLeft = zoneDownSeconds;

                        @Override
                        public void run() {
                            if (downTimeLeft <= 0) {
                                downBar.removeAll(); // Ensure the cooldown bar is removed
                                zone.endCooldown();

                                String reEnabledMsg = "&aExtraction zone is now open again!";
                                // RE-ENABLED MESSAGE: RESERVED for players inside the zone
                                messagePlayersInZone(zone, reEnabledMsg);

                                cancel();
                                return;
                            }
                            downBar.setProgress((double) downTimeLeft / zoneDownSeconds);
                            downBar.setTitle(ChatColor.RED + "Extraction down for " + downTimeLeft + "s");
                            downTimeLeft--;
                        }
                    }.runTaskTimer(plugin, 0L, 20L);
                } else {
                    portalBar.setProgress((double) timeLeft / portalOpenSeconds);
                    portalBar.setTitle(ChatColor.AQUA + "Portal open for " + timeLeft + "s");
                    timeLeft--;
                }
            }
        };

        portalTask.runTaskTimer(plugin, 0L, 20L);
    }


    // This method is the final piece of the competitive system to implement.
    private void triggerAlert(ExtractionZone zone) {
        // Implementation for the global audio/visual alert goes here,
        // referencing the zone's location (beaconLoc/particleLoc).
    }
}
