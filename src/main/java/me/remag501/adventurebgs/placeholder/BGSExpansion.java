package me.remag501.adventurebgs.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.remag501.adventurebgs.manager.RotationManager;
import me.remag501.adventurebgs.model.RotationTrack;
import me.remag501.adventurebgs.util.MessageUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BGSExpansion extends PlaceholderExpansion {

//    private final AdventureBGS plugin;
    private final RotationManager rotationManager;

    public BGSExpansion(RotationManager rotationManager) {
        this.rotationManager = rotationManager;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "adventurebgs"; // This defines the %adventurebgs_...% part
    }

    @Override
    public @NotNull String getAuthor() {
        return "remag501";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true; // Keeps the expansion loaded even if PAPI reloads
    }

//    @Override
//    public String onRequest(OfflinePlayer player, @NotNull String params) {
        // This is where the magic happens.
        // 'params' is whatever comes AFTER %adventurebgs_

//        var rotation = plugin.getRotationManager();
//
//        return switch (params.toLowerCase()) {
//            case "minutes_left" -> String.valueOf(rotation.getMinutesUntilNextCycle());
//            case "seconds_left" -> String.valueOf(rotation.getSecondsUntilNextCycle());
//            case "current_world_chat" -> MessageUtil.color(rotation.getCurrentWorld().getChatName());
//            case "next_world_chat" -> MessageUtil.color(rotation.getNextWorld().getChatName());
//            case "current_world_gui" -> MessageUtil.color(rotation.getCurrentWorld().getGuiName());
//            case "current_world_id" -> MessageUtil.color(rotation.getCurrentWorld().getId());
//            case "next_world_gui" -> MessageUtil.color(rotation.getNextWorld().getGuiName());
//            case "next_world_id" -> MessageUtil.color(rotation.getNextWorld().getId());
//            case "time_left" -> {
//                long totalSeconds = rotation.getSecondsUntilNextCycle();
//
//                long minutes = totalSeconds / 60;          // Get the whole minutes
//                long seconds = totalSeconds % 60;          // Get the remaining seconds (0-59)
//
//                // %d (minutes) = 1, 10, 100
//                // %02d (seconds) = 01, 10, 59
//                yield String.format("%d:%02d", minutes, seconds);
//            }
//            default -> null; // Return null if the placeholder isn't recognized
//        };
//    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        String lowerParams = params.toLowerCase();
        RotationTrack targetTrack = null;
        String action = lowerParams;

        // 1. Try to find a Track ID suffix
        for (RotationTrack track : rotationManager.getTracks()) {
            String idSuffix = "_" + track.getId().toLowerCase();
            if (lowerParams.endsWith(idSuffix)) {
                targetTrack = track;
                // Strip the suffix to get the clean action name
                action = lowerParams.substring(0, lowerParams.length() - idSuffix.length());
                break;
            }
        }

        // 2. If no ID suffix was matched, try to find track by Player Context
        if (targetTrack == null && offlinePlayer != null && offlinePlayer.isOnline()) {
            Player player = offlinePlayer.getPlayer();
            // Uses your helper method to find which track owns the player's current world
            targetTrack = rotationManager.getTrackByWorld(player.getWorld());
        }

        // 3. If we still don't have a track, we can't provide data
        if (targetTrack == null) return null;

        // 4. Execute the switch on the "Action" part
        return switch (action) {
            case "minutes_left" -> String.valueOf(targetTrack.getMinutesUntilNextCycle());
            case "time_left" -> {
                long totalSeconds = targetTrack.getSecondsUntilNextCycle();
                yield String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60);
            }
            case "current_world_chat" -> MessageUtil.color(targetTrack.getCurrentWorld().getChatName());
            case "next_world_chat" -> MessageUtil.color(targetTrack.getNextWorld().getChatName());
            // ... add the rest of your cases here ...
            default -> null;
        };
    }

}