package com.lovetropics.minigames.common.core.game.impl;

import com.google.common.collect.ImmutableList;
import com.lovetropics.minigames.client.data.LoveTropicsLangKeys;
import com.lovetropics.minigames.common.core.game.*;
import com.lovetropics.minigames.common.core.game.behavior.BehaviorMap;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import com.lovetropics.minigames.common.core.game.control.GameControlCommands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Unit;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;

public final class InactiveGame implements IGamePhase {
	private final IGameInstance instance;

	private final BehaviorMap emptyBehaviors = BehaviorMap.create(ImmutableList.of());
	private final GameEventListeners emptyEvents = new GameEventListeners();
	private final GameControlCommands emptyControls = new GameControlCommands(null);

	public InactiveGame(IGameInstance instance) {
		this.instance = instance;
	}

	@Override
	public IGameInstance getInstance() {
		return instance;
	}

	@Override
	public BehaviorMap getBehaviors() {
		return emptyBehaviors;
	}

	@Override
	public GameEventListeners getEvents() {
		return emptyEvents;
	}

	@Override
	public GameControlCommands getControlCommands() {
		return emptyControls;
	}

	@Override
	public PlayerSet getAllPlayers() {
		return PlayerSet.EMPTY;
	}

	@Override
	public int getMemberCount(PlayerRole role) {
		return 0;
	}

	@Override
	public GameStatus getStatus() {
		return GameStatus.INACTIVE;
	}

	@Nullable
	@Override
	public IPollingGame asPolling() {
		return null;
	}

	@Nullable
	@Override
	public IActiveGame asActive() {
		return null;
	}

	@Override
	public boolean requestPlayerJoin(ServerPlayerEntity player, @Nullable PlayerRole requestedRole) {
		return false;
	}

	@Override
	public boolean removePlayer(ServerPlayerEntity player) {
		return false;
	}

	@Override
	public GameResult<Unit> cancel() {
		return GameResult.error(new TranslationTextComponent(LoveTropicsLangKeys.COMMAND_NO_MINIGAME));
	}
}
