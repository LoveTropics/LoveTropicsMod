package com.lovetropics.minigames.common.content.build_competition;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.util.GameBehaviorEntry;
import com.lovetropics.minigames.common.util.LoveTropicsRegistrate;

public final class BuildCompetition {
	private static final LoveTropicsRegistrate REGISTRATE = LoveTropics.registrate();

	public static final GameBehaviorEntry<PollFinalistsBehavior> POLL_FINALISTS = REGISTRATE.object("poll_finalists")
			.behavior(PollFinalistsBehavior.CODEC)
			.register();

	public static void init() {
	}
}
