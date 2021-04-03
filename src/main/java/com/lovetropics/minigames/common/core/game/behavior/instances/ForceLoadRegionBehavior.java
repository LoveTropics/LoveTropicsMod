package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.minigames.common.core.game.IActiveGame;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLifecycleEvents;
import com.lovetropics.minigames.common.core.map.MapRegion;
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
	public void register(IActiveGame registerGame, EventRegistrar events) {
		acquiredChunks = acquireChunks(registerGame);

		events.listen(GameLifecycleEvents.STOP, this::onFinish);
	}

	private void onFinish(IActiveGame game) {
		ServerChunkProvider chunkProvider = game.getWorld().getChunkProvider();

		LongIterator iterator = acquiredChunks.iterator();
		while (iterator.hasNext()) {
			long chunkKey = iterator.nextLong();
			chunkProvider.forceChunk(new ChunkPos(chunkKey), false);
		}
	}

	private LongSet acquireChunks(IActiveGame game) {
		ServerChunkProvider chunkProvider = game.getWorld().getChunkProvider();

		LongSet chunks = collectChunks(game);

		LongIterator iterator = chunks.iterator();
		while (iterator.hasNext()) {
			long chunkKey = iterator.nextLong();
			chunkProvider.forceChunk(new ChunkPos(chunkKey), true);
		}

		iterator = chunks.iterator();
		while (iterator.hasNext()) {
			long chunkKey = iterator.nextLong();
			chunkProvider.getChunk(ChunkPos.getX(chunkKey), ChunkPos.getZ(chunkKey), true);
		}

		return chunks;
	}

	private LongSet collectChunks(IActiveGame minigame) {
		LongSet chunks = new LongOpenHashSet();

		Collection<MapRegion> regions = minigame.getMapRegions().get(regionKey);
		for (MapRegion region : regions) {
			chunks.addAll(region.asChunks());
		}

		return chunks;
	}
}
