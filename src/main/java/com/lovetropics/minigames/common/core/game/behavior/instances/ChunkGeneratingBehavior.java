package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.server.ServerWorld;

public abstract class ChunkGeneratingBehavior implements IGameBehavior {
	private final LongSet generatedChunks = new LongOpenHashSet();

	protected abstract void generateChunk(IGameInstance minigame, ServerWorld world, Chunk chunk);

	@Override
	public final void onChunkLoad(IGameInstance minigame, IChunk chunk) {
		if (chunk instanceof Chunk && generatedChunks.add(chunk.getPos().asLong())) {
			generateChunk(minigame, minigame.getWorld(), (Chunk) chunk);
		}
	}
}
