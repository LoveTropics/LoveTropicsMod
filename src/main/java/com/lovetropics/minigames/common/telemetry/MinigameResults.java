package com.lovetropics.minigames.common.telemetry;

import com.google.gson.annotations.SerializedName;
import com.lovetropics.minigames.common.minigames.statistics.MinigameStatistics;
import com.lovetropics.minigames.common.minigames.statistics.PlayerKey;

public class MinigameResults {
    private String id;
    /** Event name */
    private String name;
    private PlayerKey initiator;
    /** The statistics from this minigame */
    private MinigameStatistics statistics;
    @SerializedName("finish_timestamp_utc")
    private long finishTimestampUtc;

    public MinigameResults() {
    }

    public MinigameResults(String id, String name, PlayerKey initiator, MinigameStatistics statistics, long finishTimestampUtc) {
        this.id = id;
        this.name = name;
        this.initiator = initiator;
        this.statistics = statistics;
        this.finishTimestampUtc = finishTimestampUtc;
    }
}
