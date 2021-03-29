package com.lovetropics.minigames.common.core.dimension;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.listener.IChunkStatusListener;

import javax.annotation.Nullable;

public final class VoidChunkStatusListener implements IChunkStatusListener {
	public static final VoidChunkStatusListener INSTANCE = new VoidChunkStatusListener();

	private VoidChunkStatusListener() {
	}

	@Override
	public void start(ChunkPos spawnPos) {
	}

	@Override
	public void statusChanged(ChunkPos pos, @Nullable ChunkStatus newStatus) {
	}

	@Override
	public void stop() {
	}
}
