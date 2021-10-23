package com.lovetropics.minigames.common.content.hide_and_seek;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.util.registry.GameBehaviorEntry;
import com.lovetropics.minigames.common.util.registry.LoveTropicsRegistrate;
import com.tterrag.registrate.util.entry.ItemEntry;

public final class HideAndSeek {
	private static final LoveTropicsRegistrate REGISTRATE = LoveTropics.registrate();

	public static final ItemEntry<NetItem> NET = REGISTRATE.item("net", NetItem::new)
			.register();

	public static final GameBehaviorEntry<HideAndSeekBehavior> HIDE_AND_SEEK = REGISTRATE.object("hide_and_seek")
			.behavior(HideAndSeekBehavior.CODEC)
			.register();

	public static void init() {
	}
}
