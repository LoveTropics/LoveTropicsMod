package com.lovetropics.minigames.common.minigames.behaviours.instances;

import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.server.ServerWorld;

public abstract class ChunkGeneratingBehavior implements IMinigameBehavior {
	private final LongSet generatedChunks = new LongOpenHashSet();

	protected abstract void generateChunk(IMinigameInstance minigame, ServerWorld world, Chunk chunk);

	@Override
	public final void onChunkLoad(IMinigameInstance minigame, IChunk chunk) {
		if (chunk instanceof Chunk && generatedChunks.add(chunk.getPos().asLong())) {
			generateChunk(minigame, minigame.getWorld(), (Chunk) chunk);
		}
	}
}
