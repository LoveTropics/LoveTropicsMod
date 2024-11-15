package com.lovetropics.minigames.common.content.de_a_coudre;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.util.registry.GameBehaviorEntry;
import com.lovetropics.minigames.common.util.registry.LoveTropicsRegistrate;

public class DeACoudre {
	private static final LoveTropicsRegistrate REGISTRATE = LoveTropics.registrate();

	public static final GameBehaviorEntry<DeACoudreBehavior> BEHAVIOR = REGISTRATE.object("de_a_coudre")
			.behavior(DeACoudreBehavior.CODEC)
			.register();

	public static void init() {
	}
}
