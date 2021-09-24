package com.lovetropics.minigames.common.core.game.behavior.event;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import net.minecraft.entity.player.ServerPlayerEntity;

import javax.annotation.Nullable;

public final class GameWaitingEvents {
	public static final GameEventType<PlayerWaiting> PLAYER_WAITING = GameEventType.create(PlayerWaiting.class, listeners -> (game, player, role) -> {
		for (PlayerWaiting listener : listeners) {
			listener.onPlayerWaiting(game, player, role);
		}
	});

	private GameWaitingEvents() {
	}

	public interface PlayerWaiting {
		void onPlayerWaiting(IGamePhase game, ServerPlayerEntity player, @Nullable PlayerRole role);
	}
}
