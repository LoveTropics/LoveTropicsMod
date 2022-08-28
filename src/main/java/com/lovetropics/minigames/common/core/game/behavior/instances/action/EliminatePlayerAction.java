package com.lovetropics.minigames.common.core.game.behavior.instances.action;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameActionEvents;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.mojang.serialization.Codec;

public final class EliminatePlayerAction implements IGameBehavior {
	public static final Codec<EliminatePlayerAction> CODEC = Codec.unit(EliminatePlayerAction::new);

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GameActionEvents.APPLY_TO_PLAYER, (context, target) -> {
			if (!game.getSpectators().contains(target)) {
				game.setPlayerRole(target, PlayerRole.SPECTATOR);
				target.setHealth(20.0F);
			}
			return true;
		});
	}
}
