package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameWorldEvents;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;

public abstract class ChunkGeneratingBehavior implements IGameBehavior {
	private final LongSet generatedChunks = new LongOpenHashSet();

	@Override
	public void register(IGameInstance registerGame, EventRegistrar events) {
		events.listen(GameWorldEvents.CHUNK_LOAD, (game, chunk) -> {
			if (chunk instanceof Chunk && generatedChunks.add(chunk.getPos().asLong())) {
				generateChunk(game, game.getWorld(), (Chunk) chunk);
			}
		});
	}

	protected abstract void generateChunk(IGameInstance game, ServerWorld world, Chunk chunk);
}
