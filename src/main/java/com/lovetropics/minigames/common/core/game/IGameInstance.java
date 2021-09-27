package com.lovetropics.minigames.common.core.game;

import net.minecraft.util.Unit;

import javax.annotation.Nullable;

public interface IGameInstance extends IProtoGame {
	@Nullable
	IGamePhase getCurrentPhase();

	GameResult<Unit> stop(GameStopReason reason);
}
