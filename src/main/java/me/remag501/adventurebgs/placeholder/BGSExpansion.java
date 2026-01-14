package me.remag501.adventurebgs.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.remag501.adventurebgs.AdventureBGS;
import me.remag501.adventurebgs.util.MessageUtil;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class BGSExpansion extends PlaceholderExpansion {

    private final AdventureBGS plugin;

    public BGSExpansion(AdventureBGS plugin) {
        this.plugin = plugin;
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
}