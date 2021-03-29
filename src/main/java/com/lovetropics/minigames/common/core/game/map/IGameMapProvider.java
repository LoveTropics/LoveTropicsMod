package com.lovetropics.minigames.common.core.game.map;

import com.lovetropics.minigames.common.core.game.GameResult;
import com.mojang.serialization.Codec;
import net.minecraft.server.MinecraftServer;

import java.util.concurrent.CompletableFuture;

public interface IGameMapProvider {
	Codec<? extends IGameMapProvider> getCodec();

	CompletableFuture<GameResult<GameMap>> open(MinecraftServer server);
}
