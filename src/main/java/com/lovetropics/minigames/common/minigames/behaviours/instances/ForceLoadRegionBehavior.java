package com.lovetropics.minigames.common.minigames.behaviours.instances;

import com.lovetropics.minigames.common.map.MapRegion;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.mojang.datafixers.Dynamic;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.server.ServerWorld;

import java.util.Collection;

public final class ForceLoadRegionBehavior implements IMinigameBehavior {
	private final String regionKey;
	private LongSet acquiredChunks;

	public ForceLoadRegionBehavior(String regionKey) {
		this.regionKey = regionKey;
	}

	public static <T> ForceLoadRegionBehavior parse(Dynamic<T> root) {
		String region = root.get("region").asString("");
		return new ForceLoadRegionBehavior(region);
	}

	@Override
	public void onConstruct(IMinigameInstance minigame) {
		ServerWorld world = minigame.getWorld();

		LongSet chunks = collectChunks(minigame);

		LongIterator iterator = chunks.iterator();
		while (iterator.hasNext()) {
			long chunkKey = iterator.nextLong();
			world.forceChunk(ChunkPos.getX(chunkKey), ChunkPos.getZ(chunkKey), true);
		}

		acquiredChunks = chunks;
	}

	@Override
	public void onFinish(IMinigameInstance minigame) {
		ServerWorld world = minigame.getWorld();

		LongIterator iterator = acquiredChunks.iterator();
		while (iterator.hasNext()) {
			long chunkKey = iterator.nextLong();
			world.forceChunk(ChunkPos.getX(chunkKey), ChunkPos.getZ(chunkKey), false);
		}
	}

	private LongSet collectChunks(IMinigameInstance minigame) {
		LongSet chunks = new LongOpenHashSet();

		Collection<MapRegion> regions = minigame.getMapRegions().get(regionKey);
		for (MapRegion region : regions) {
			chunks.addAll(region.asChunks());
		}

		return chunks;
	}
}
