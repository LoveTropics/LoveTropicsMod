package com.lovetropics.minigames.common.core.dimension;

import net.minecraft.world.Dimension;
import net.minecraft.world.storage.IServerWorldInfo;

public final class RuntimeDimensionConfig {
	public final Dimension dimension;
	public final long seed;
	public final IServerWorldInfo worldInfo;

	public RuntimeDimensionConfig(Dimension dimension, long seed, IServerWorldInfo worldInfo) {
		this.dimension = dimension;
		this.seed = seed;
		this.worldInfo = worldInfo;
	}
}
