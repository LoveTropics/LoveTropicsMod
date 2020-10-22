package com.lovetropics.minigames.common.techstack;

import com.lovetropics.minigames.common.minigames.statistics.MinigameStatistics;

public class MinigameResult {
    /** Event name */
    private String name;
    /** The statistics from this minigame */
    private MinigameStatistics statistics;

    public MinigameResult() {
    }

    public MinigameResult(String name, MinigameStatistics statistics) {
        this.name = name;
        this.statistics = statistics;
    }
}
