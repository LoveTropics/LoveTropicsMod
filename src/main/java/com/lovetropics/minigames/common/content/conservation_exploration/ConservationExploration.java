package com.lovetropics.minigames.common.content.conservation_exploration;

import com.lovetropics.minigames.LoveTropics;
import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.util.entry.ItemEntry;

public final class ConservationExploration {
	private static final Registrate REGISTRATE = LoveTropics.registrate();

	public static final ItemEntry<RecordCreatureItem> RECORD_CREATURE = REGISTRATE.item("record_creature", RecordCreatureItem::new)
			.register();

	public static void init() {}
}
