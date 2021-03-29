package com.lovetropics.minigames.common.minigames.map;

import com.lovetropics.minigames.common.minigames.MinigameMap;
import com.lovetropics.minigames.common.minigames.MinigameResult;
import com.mojang.serialization.Codec;
import net.minecraft.server.MinecraftServer;

import java.util.concurrent.CompletableFuture;

public interface IMinigameMapProvider {
	Codec<? extends IMinigameMapProvider> getCodec();

	CompletableFuture<MinigameResult<MinigameMap>> open(MinecraftServer server);
}
