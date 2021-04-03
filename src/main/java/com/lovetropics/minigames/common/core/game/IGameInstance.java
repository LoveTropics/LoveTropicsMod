package com.lovetropics.minigames.common.core.game;

import com.lovetropics.minigames.common.core.game.behavior.BehaviorMap;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import com.lovetropics.minigames.common.core.game.control.GameControlCommands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Unit;

import javax.annotation.Nullable;

public interface IGameInstance extends IProtoGame {
	IGamePhase getPhase();

	@Override
	default GameResult<Unit> cancel() {
		return getPhase().cancel();
	}

	@Override
	default BehaviorMap getBehaviors() {
		return getPhase().getBehaviors();
	}

	@Override
	default GameEventListeners getEvents() {
		return getPhase().getEvents();
	}

	@Override
	default GameControlCommands getControlCommands() {
		return getPhase().getControlCommands();
	}

	@Override
	default GameStatus getStatus() {
		return getPhase().getStatus();
	}

	@Override
	default PlayerSet getAllPlayers() {
		return getPhase().getAllPlayers();
	}

	@Override
	default boolean requestPlayerJoin(ServerPlayerEntity player, @Nullable PlayerRole requestedRole) {
		return getPhase().requestPlayerJoin(player, requestedRole);
	}

	@Override
	default boolean removePlayer(ServerPlayerEntity player) {
		return getPhase().removePlayer(player);
	}

	@Override
	default int getMemberCount(PlayerRole role) {
		return getPhase().getMemberCount(role);
	}

	@Override
	@Nullable
	default IPollingGame asPolling() {
		return getPhase().asPolling();
	}

	@Override
	@Nullable
	default IActiveGame asActive() {
		return getPhase().asActive();
	}
}
