package me.remag501.adventurebgs.model;

public class WeatherModel {

    private final String type;
    private final String world;

    private final int minDurationSeconds;
    private final int maxDurationSeconds;

    private final int minFrequencyMinutes;
    private final int maxFrequencyMinutes;

    public WeatherModel(
            String type,
            String world,
            int minDurationSeconds,
            int maxDurationSeconds,
            int minFrequencyMinutes,
            int maxFrequencyMinutes
    ) {
        this.type = type;
        this.world = world;
        this.minDurationSeconds = minDurationSeconds;
        this.maxDurationSeconds = maxDurationSeconds;
        this.minFrequencyMinutes = minFrequencyMinutes;
        this.maxFrequencyMinutes = maxFrequencyMinutes;
    }

    public String getType() { return type; }
    public String getWorld() { return world; }

    public int randomDurationSeconds() {
        return minDurationSeconds +
                (int) (Math.random() * (maxDurationSeconds - minDurationSeconds + 1));
    }

    public int randomFrequencyMinutes() {
        return minFrequencyMinutes +
                (int) (Math.random() * (maxFrequencyMinutes - minFrequencyMinutes + 1));
    }
}
