package com.lovetropics.minigames.common.techstack;

import java.util.List;

public class MinigameResult {
    /** Event name */
    private String name;
    /** Host of event */
    private String host;
    /** List of participants */
    private List<ParticipantEntry> participants;

    public MinigameResult() {
    }

    public MinigameResult(String name, String host, List<ParticipantEntry> participants) {
        this.name = name;
        this.host = host;
        this.participants = participants;
    }
}
