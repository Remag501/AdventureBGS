package me.remag501.adventurebgs.listeners;

import me.remag501.adventurebgs.AdventureBGS;
import me.remag501.adventurebgs.managers.ExtractionManager;
import me.remag501.adventurebgs.model.ExtractionZone;
import me.remag501.adventurebgs.util.MessageUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
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

    public ExtractionListener(AdventureBGS plugin) {
        this.plugin = plugin;
        this.manager = plugin.getExtractionManager();
    }

    /**
     * Changes the color of the two glass blocks above the beacon for a visual indicator.
     */
    private void updateBeaconColor(ExtractionZone zone, Material glassType1, Material glassType2) {
        Location beaconLoc = zone.getBeaconLoc();
        if (beaconLoc == null) return;

        World world = beaconLoc.getWorld();
        if (world == null) return;

        int chunkX = beaconLoc.getBlockX() >> 4;
        int chunkZ = beaconLoc.getBlockZ() >> 4;

        world.getChunkAtAsync(chunkX, chunkZ, /* generate */ false, chunk -> {
            // Runs once the chunk is loaded — **without blocking the main thread**
            // Now you can safely update blocks inside a Bukkit task
            Bukkit.getScheduler().runTask(plugin, () -> {
                Block glass1 = beaconLoc.clone().add(0, 1, 0).getBlock();
                Block glass2 = beaconLoc.clone().add(0, 2, 0).getBlock();
                glass1.setType(glassType1, false);
                glass2.setType(glassType2, false);
            });
        });
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
                    String cancelMessage = MessageUtil.color(plugin.getSettings().getExtractionCancel());

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
        String rawStartMsg = plugin.getSettings().getExtractionStart();
        String zoneNamePlaceholder = "Extraction Zone";
        String startMsg = MessageUtil.color(rawStartMsg
                .replace("%seconds%", String.valueOf(plugin.getSettings().getExtractionDuration()))
                .replace("%zone%", zoneNamePlaceholder));

        // Message restricted to players currently in the zone
        messagePlayersInZone(zone, startMsg);

        String title = MessageUtil.color(plugin.getSettings().getExtractionBossTitle());

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
            int timeLeft = plugin.getSettings().getExtractionDuration();
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

                int alertSeconds = plugin.getSettings().getAlertSeconds();
                if (timeLeft == alertSeconds && !alertTriggered) {
                    // IMPLEMENTED: Global Alert System call
                    triggerAlert(zone);
                    alertTriggered = true;
                }

                zone.getExtractionBossBar().setProgress((double) timeLeft / plugin.getSettings().getExtractionDuration());
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

        String successMsg = MessageUtil.color(plugin.getSettings().getExtractionSuccess());

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

        // BEACON UPDATE: Portal is open -> Yellow
        updateBeaconColor(zone, Material.AIR, Material.AIR);

        // --- Portal open phase ---
        int portalOpenSeconds = plugin.getSettings().getPortalOpenSeconds();
        String portalOpenMessage = plugin.getSettings().getExtractionPortalOpen()
                .replace("%seconds%", String.valueOf(portalOpenSeconds));

        // PORTAL OPEN MESSAGE: RESERVED for players *currently* inside the zone (excludes successful extractors)
        messagePlayersInZone(zone, portalOpenMessage);

        BossBar portalBar = Bukkit.createBossBar(
                "§aPortal is open for §a§l" + portalOpenSeconds + " §aseconds...",
                BarColor.GREEN,
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
                    int zoneDownSeconds = plugin.getSettings().getDownSeconds();
                    String zoneDownMsg = plugin.getSettings().getExtractionDown()
                            .replace("%seconds%", String.valueOf(zoneDownSeconds));

                    // ZONE DOWN MESSAGE: RESERVED for players inside the zone
                    messagePlayersInZone(zone, zoneDownMsg);

                    // BEACON UPDATE: Zone is down -> Red
                    updateBeaconColor(zone, Material.RED_STAINED_GLASS, Material.RED_STAINED_GLASS);

                    BossBar downBar = Bukkit.createBossBar(
                            "§cExtraction is down for §c§l" + zoneDownSeconds + " §cseconds...",
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

                                // BEACON UPDATE: Zone is ready -> Green
                                updateBeaconColor(zone, Material.LIME_STAINED_GLASS, Material.LIME_STAINED_GLASS);

                                String reEnabledMsg = "&aExtraction zone is now open again!";
                                // RE-ENABLED MESSAGE: RESERVED for players inside the zone
                                messagePlayersInZone(zone, reEnabledMsg);

                                cancel();
                                return;
                            }
                            downBar.setProgress((double) downTimeLeft / zoneDownSeconds);
                            downBar.setTitle("§cExtraction is down for §c§l" + downTimeLeft + " §cseconds...");
                            downTimeLeft--;
                        }
                    }.runTaskTimer(plugin, 0L, 20L);
                } else {
                    portalBar.setProgress((double) timeLeft / portalOpenSeconds);
                    portalBar.setTitle("§aPortal is open for §a§l" + timeLeft + " §aseconds...");
                    timeLeft--;
                }
            }
        };

        portalTask.runTaskTimer(plugin, 0L, 20L);
    }


    /**
     * Triggers a global alert (sound, particles) to warn all players in the world
     * that an extraction is about to be completed.
     */
    private void triggerAlert(ExtractionZone zone) {
        // 1. Fetch config values for the alert
        String rawSound = plugin.getSettings().getAlertSound();

        World world = Bukkit.getWorld(zone.getWorld());
        if (world == null) return;

        // 2. Play sound to ALL players in the extraction world
        try {
            Sound alertSound = Sound.valueOf(rawSound.toUpperCase());
            for (Player p : world.getPlayers()) {
                // Play sound localized to the zone's beacon/particle location
                Location soundLoc = zone.getBeaconLoc() != null ? zone.getBeaconLoc() : p.getLocation();
                p.playSound(soundLoc, alertSound, 2.0f, 1.0f);
            }
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound configured for extraction alert: " + rawSound);
        }

        // 3. Spawn particles at the specified location (beacon or particle location)
        Location particleLoc = zone.getParticleLoc() != null ? zone.getParticleLoc() : zone.getBeaconLoc();
        if (particleLoc != null) {
            // Create a burst of particles simulating a flare or fireworks to draw attention

            // Large upward visual flare (Fireworks Spark)
            world.spawnParticle(Particle.FIREWORKS_SPARK, particleLoc, 100, 0.5, 0.5, 0.5, 0.5);

            // Colored smoke/dust trail for drama (Green and Yellow)
            world.spawnParticle(Particle.REDSTONE, particleLoc, 50, 1.0, 1.0, 1.0, 0.0, new Particle.DustOptions(Color.LIME, 1.5f));
            world.spawnParticle(Particle.REDSTONE, particleLoc, 50, 1.0, 1.0, 1.0, 0.0, new Particle.DustOptions(Color.YELLOW, 1.5f));

            // Instantaneous large explosion effect for emphasis
            world.spawnParticle(Particle.EXPLOSION_LARGE, particleLoc, 1, 0, 0, 0, 0);
        }
    }
}
