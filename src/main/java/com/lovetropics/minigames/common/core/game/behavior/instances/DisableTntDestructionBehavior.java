package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import com.lovetropics.minigames.common.core.game.behavior.event.GameWorldEvents;
import com.mojang.serialization.Codec;

public class DisableTntDestructionBehavior implements IGameBehavior {
	public static final Codec<DisableTntDestructionBehavior> CODEC = Codec.unit(DisableTntDestructionBehavior::new);

	@Override
	public void register(IGameInstance registerGame, GameEventListeners events) {
		events.listen(GameWorldEvents.EXPLOSION_DETONATE, (game, explosion, affectedBlocks, affectedEntities) -> {
			affectedBlocks.clear();
		});
	}
}
