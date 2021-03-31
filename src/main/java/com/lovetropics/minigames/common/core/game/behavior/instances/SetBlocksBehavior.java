package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLifecycleEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GameWorldEvents;
import com.lovetropics.minigames.common.core.map.MapRegion;
import com.lovetropics.minigames.common.util.MoreCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import net.minecraft.advancements.criterion.BlockPredicate;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.gen.blockstateprovider.BlockStateProvider;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.*;

public final class SetBlocksBehavior implements IGameBehavior {
	public static final Codec<SetBlocksBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				MoreCodecs.BLOCK_PREDICATE.optionalFieldOf("replace").forGetter(c -> Optional.ofNullable(c.replace)),
				MoreCodecs.BLOCK_STATE_PROVIDER.fieldOf("set").forGetter(c -> c.set),
				Codec.STRING.optionalFieldOf("region").forGetter(c -> Optional.ofNullable(c.regionKey)),
				Codec.LONG.optionalFieldOf("time").forGetter(c -> c.time != -1 ? Optional.of(c.time) : Optional.empty())
		).apply(instance, SetBlocksBehavior::new);
	});

	private final @Nullable BlockPredicate replace;
	private final BlockStateProvider set;

	private final @Nullable String regionKey;
	private final long time;

	private Collection<MapRegion> regions;

	private SetBlocksBehavior(Optional<BlockPredicate> replace, BlockStateProvider set, Optional<String> regionKey, Optional<Long> time) {
		this(replace.orElse(null), set, regionKey.orElse(null), time.orElse(-1L));
	}

	public SetBlocksBehavior(BlockPredicate replace, BlockStateProvider set, @Nullable String regionKey, long time) {
		this.replace = replace;
		this.set = set;
		this.regionKey = regionKey;
		this.time = time;
	}

	@Override
	public void register(IGameInstance registerGame, GameEventListeners events) {
		regions = regionKey != null ? registerGame.getMapRegions().get(regionKey) : null;

		if (time != -1) {
			registerTimed(events);
		} else {
			registerImmediate(events);
		}
	}

	private void registerTimed(GameEventListeners events) {
		Collection<MapRegion> regions = this.regions;
		if (regions == null) {
			throw new GameException(new StringTextComponent("Regions not specified for block set behavior with a set time!"));
		}

		events.listen(GameLifecycleEvents.TICK, game -> {
			if (time != game.ticks()) return;

			for (MapRegion region : regions) {
				setInRegion(game, region);
			}
		});
	}

	private void registerImmediate(GameEventListeners events) {
		if (regions != null) {
			Long2ObjectMap<List<MapRegion>> regionsByChunk = collectRegionsByChunk(regions);

			events.listen(GameWorldEvents.CHUNK_LOAD, (game, chunk) -> {
				List<MapRegion> regions = regionsByChunk.remove(chunk.getPos().asLong());
				if (regions == null) {
					return;
				}

				for (MapRegion region : regions) {
					setInRegion(game, region);
				}
			});
		} else {
			events.listen(GameWorldEvents.CHUNK_LOAD, (game, chunk) -> {
				MapRegion region = MapRegion.ofChunk(chunk.getPos());
				setInRegion(game, region);
			});
		}
	}

	private static Long2ObjectMap<List<MapRegion>> collectRegionsByChunk(Collection<MapRegion> regions) {
		Long2ObjectMap<List<MapRegion>> regionsByChunk = new Long2ObjectOpenHashMap<>();

		for (MapRegion region : regions) {
			LongIterator chunkIterator = region.asChunks().iterator();
			while (chunkIterator.hasNext()) {
				long chunkPos = chunkIterator.nextLong();
				MapRegion chunkRegion = MapRegion.ofChunk(ChunkPos.getX(chunkPos), ChunkPos.getZ(chunkPos));

				MapRegion intersectionRegion = region.intersection(chunkRegion);
				if (intersectionRegion != null) {
					regionsByChunk.computeIfAbsent(chunkPos, l -> new ArrayList<>()).add(intersectionRegion);
				}
			}
		}

		return regionsByChunk;
	}

	private void setInRegion(IGameInstance game, MapRegion region) {
		ServerWorld world = game.getWorld();
		BlockPredicate replace = this.replace;
		BlockStateProvider set = this.set;
		Random random = world.rand;

		for (BlockPos pos : region) {
			if (replace == null || replace.test(world, pos)) {
				BlockState state = set.getBlockState(random, pos);
				world.setBlockState(pos, state);
			}
		}
	}
}
