package com.lovetropics.minigames.common.core.game.behavior.instances.tweak;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameWorldEvents;
import com.mojang.serialization.Codec;

public class DisableTntDestructionBehavior implements IGameBehavior {
	public static final Codec<DisableTntDestructionBehavior> CODEC = Codec.unit(DisableTntDestructionBehavior::new);

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GameWorldEvents.EXPLOSION_DETONATE, (explosion, affectedBlocks, affectedEntities) -> {
			affectedBlocks.clear();
		});
	}
}
