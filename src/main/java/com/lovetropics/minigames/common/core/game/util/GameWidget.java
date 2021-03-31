package com.lovetropics.minigames.common.core.game.util;

import net.minecraft.entity.player.ServerPlayerEntity;

public interface GameWidget extends AutoCloseable {
	void addPlayer(ServerPlayerEntity player);

	void removePlayer(ServerPlayerEntity player);

	@Override
	void close();
}
