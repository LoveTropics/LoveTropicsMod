package com.lovetropics.minigames.common.content.conservation_exploration;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.util.GameBehaviorEntry;
import com.lovetropics.minigames.common.util.LoveTropicsRegistrate;
import com.tterrag.registrate.util.entry.ItemEntry;

public final class ConservationExploration {
	private static final LoveTropicsRegistrate REGISTRATE = LoveTropics.registrate();

	public static final ItemEntry<RecordCreatureItem> RECORD_CREATURE = REGISTRATE.item("record_creature", RecordCreatureItem::new)
			.register();

	public static final GameBehaviorEntry<ConservationExplorationBehavior> CONSERVATION_EXPLORATION = REGISTRATE.object("conservation_exploration")
			.behavior(ConservationExplorationBehavior.CODEC)
			.register();

	public static void init() {
	}
}
