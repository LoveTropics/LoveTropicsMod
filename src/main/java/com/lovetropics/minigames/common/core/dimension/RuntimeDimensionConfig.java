package com.lovetropics.minigames.common.core.dimension;

import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.ServerLevelData;

public final class RuntimeDimensionConfig {
	public final LevelStem dimension;
	public final long seed;
	public final ServerLevelData worldInfo;

	public RuntimeDimensionConfig(LevelStem dimension, long seed, ServerLevelData worldInfo) {
		this.dimension = dimension;
		this.seed = seed;
		this.worldInfo = worldInfo;
	}
}
