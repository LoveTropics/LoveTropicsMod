package com.lovetropics.minigames.common.core.game.behavior.event;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.chunk.IChunk;

import java.util.List;

public final class GameWorldEvents {
	public static final GameEventType<ChunkLoad> CHUNK_LOAD = GameEventType.create(ChunkLoad.class, listeners -> (chunk) -> {
		for (ChunkLoad listener : listeners) {
			listener.onChunkLoad(chunk);
		}
	});

	public static final GameEventType<ExplosionDetonate> EXPLOSION_DETONATE = GameEventType.create(ExplosionDetonate.class, listeners -> (explosion, affectedBlocks, affectedEntities) -> {
		for (ExplosionDetonate listener : listeners) {
			listener.onExplosionDetonate(explosion, affectedBlocks, affectedEntities);
		}
	});

	private GameWorldEvents() {
	}

	public interface ChunkLoad {
		void onChunkLoad(IChunk chunk);
	}

	public interface ExplosionDetonate {
		void onExplosionDetonate(Explosion explosion, List<BlockPos> affectedBlocks, List<Entity> affectedEntities);
	}
}
