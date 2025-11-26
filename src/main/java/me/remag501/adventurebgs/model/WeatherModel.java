package me.remag501.adventurebgs.model;

public class WeatherModel {

    private final String type;
    private final String world;

    private final int minDurationSeconds;
    private final int maxDurationSeconds;

    private final int minFrequencySeconds;
    private final int maxFrequencySeconds;

    public WeatherModel(
            String type,
            String world,
            int minDurationSeconds,
            int maxDurationSeconds,
            int minFrequencySeconds,
            int maxFrequencySeconds
    ) {
        this.type = type;
        this.world = world;
        this.minDurationSeconds = minDurationSeconds;
        this.maxDurationSeconds = maxDurationSeconds;
        this.minFrequencySeconds = minFrequencySeconds;
        this.maxFrequencySeconds = maxFrequencySeconds;
    }

    public String getType() {
        return type;
    }

    public String getWorld() {
        return world;
    }

    public int getMinDurationSeconds() {
        return minDurationSeconds;
    }

    public int getMaxDurationSeconds() {
        return maxDurationSeconds;
    }

    public int getMinFrequencySeconds() {
        return minFrequencySeconds;
    }

    public int getMaxFrequencySeconds() {
        return maxFrequencySeconds;
    }
}
