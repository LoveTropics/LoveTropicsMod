package com.lovetropics.minigames.common.core.game;

import net.minecraft.server.level.ServerPlayer;

public interface PlayerListAccess {
	void ltminigames$clear(ServerPlayer player);

	void ltminigames$save(ServerPlayer player);
}
