package com.lovetropics.minigames.common.core.game.behavior.instances.action;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameActionEvents;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.mojang.serialization.MapCodec;

public final class EliminatePlayerAction implements IGameBehavior {
	public static final MapCodec<EliminatePlayerAction> CODEC = MapCodec.unit(EliminatePlayerAction::new);

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GameActionEvents.APPLY_TO_PLAYER, (context, target) -> {
			if (!game.spectators().contains(target)) {
				game.setPlayerRole(target, PlayerRole.SPECTATOR);
				target.setHealth(20.0F);
			}
			return true;
		});
	}
}
