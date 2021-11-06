package com.lovetropics.minigames.common.content.block_party;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.util.registry.GameBehaviorEntry;
import com.lovetropics.minigames.common.util.registry.LoveTropicsRegistrate;

public final class BlockParty {
	private static final LoveTropicsRegistrate REGISTRATE = LoveTropics.registrate();

	public static final GameBehaviorEntry<BlockPartyBehavior> BLOCK_PARTY = REGISTRATE.object("block_party")
			.behavior(BlockPartyBehavior.CODEC)
			.register();

	public static void init() {
	}
}
