package com.lovetropics.minigames.common.core.game;

import javax.annotation.Nullable;

public interface IGameInstance extends IProtoGame {
	@Nullable
	IGamePhase getCurrentPhase();
}
