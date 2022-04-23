package com.lovetropics.minigames.common.core.game.map;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.lovetropics.minigames.common.core.game.GameResult;
import com.mojang.serialization.Codec;

import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public interface IGameMapProvider {
	Codec<? extends IGameMapProvider> getCodec();

	CompletableFuture<GameResult<GameMap>> open(MinecraftServer server);

	default List<ResourceKey<Level>> getPossibleDimensions() {
		return Collections.emptyList();
	}
}
