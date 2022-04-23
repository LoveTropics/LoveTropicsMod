package com.lovetropics.minigames.common.core.game.behavior.instances.world;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.server.ServerChunkProvider;

import java.util.Collection;

public final class ForceLoadRegionBehavior implements IGameBehavior {
	public static final Codec<ForceLoadRegionBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.STRING.fieldOf("region").forGetter(c -> c.regionKey)
		).apply(instance, ForceLoadRegionBehavior::new);
	});

	private final String regionKey;
	private LongSet acquiredChunks;

	public ForceLoadRegionBehavior(String regionKey) {
		this.regionKey = regionKey;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		acquiredChunks = acquireChunks(game);
		events.listen(GamePhaseEvents.STOP, reason -> onStop(game));
	}

	private void onStop(IGamePhase game) {
		ServerChunkProvider chunkProvider = game.getWorld().getChunkSource();

		LongIterator iterator = acquiredChunks.iterator();
		while (iterator.hasNext()) {
			long chunkKey = iterator.nextLong();
			chunkProvider.updateChunkForced(new ChunkPos(chunkKey), false);
		}
	}

	private LongSet acquireChunks(IGamePhase game) {
		ServerChunkProvider chunkProvider = game.getWorld().getChunkSource();

		LongSet chunks = collectChunks(game);

		LongIterator iterator = chunks.iterator();
		while (iterator.hasNext()) {
			long chunkKey = iterator.nextLong();
			chunkProvider.updateChunkForced(new ChunkPos(chunkKey), true);
		}

		iterator = chunks.iterator();
		while (iterator.hasNext()) {
			long chunkKey = iterator.nextLong();
			chunkProvider.getChunk(ChunkPos.getX(chunkKey), ChunkPos.getZ(chunkKey), true);
		}

		return chunks;
	}

	private LongSet collectChunks(IGamePhase game) {
		LongSet chunks = new LongOpenHashSet();

		Collection<BlockBox> regions = game.getMapRegions().get(regionKey);
		for (BlockBox region : regions) {
			chunks.addAll(region.asChunks());
		}

		return chunks;
	}
}
