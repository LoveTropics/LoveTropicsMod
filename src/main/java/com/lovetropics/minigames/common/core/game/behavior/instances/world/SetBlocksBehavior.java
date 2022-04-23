package com.lovetropics.minigames.common.core.game.behavior.instances.world;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GameWorldEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.*;

public final class SetBlocksBehavior implements IGameBehavior {
	public static final Codec<SetBlocksBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				MoreCodecs.BLOCK_PREDICATE.optionalFieldOf("replace").forGetter(c -> Optional.ofNullable(c.replace)),
				MoreCodecs.BLOCK_STATE_PROVIDER.fieldOf("set").forGetter(c -> c.set),
				MoreCodecs.arrayOrUnit(Codec.STRING, String[]::new).optionalFieldOf("region", new String[0]).forGetter(c -> c.regionKeys),
				Codec.LONG.optionalFieldOf("time").forGetter(c -> c.time != -1 ? Optional.of(c.time) : Optional.empty()),
				Codec.BOOL.optionalFieldOf("notify_neighbors", true).forGetter(c -> c.notifyNeighbors)
		).apply(instance, SetBlocksBehavior::new);
	});

	private final @Nullable BlockPredicate replace;
	private final BlockStateProvider set;

	private final String[] regionKeys;
	private final long time;

	private final boolean notifyNeighbors;

	private Collection<BlockBox> regions;

	private SetBlocksBehavior(Optional<BlockPredicate> replace, BlockStateProvider set, String[] regionKeys, Optional<Long> time, boolean notifyNeighbors) {
		this(replace.orElse(null), set, regionKeys, time.orElse(-1L), notifyNeighbors);
	}

	public SetBlocksBehavior(BlockPredicate replace, BlockStateProvider set, String[] regionKeys, long time, boolean notifyNeighbors) {
		this.replace = replace;
		this.set = set;
		this.regionKeys = regionKeys;
		this.time = time;
		this.notifyNeighbors = notifyNeighbors;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		List<BlockBox> regions = new ArrayList<>();
		for (String regionKey : regionKeys) {
			regions.addAll(game.getMapRegions().get(regionKey));
		}

		this.regions = !regions.isEmpty() ? regions : null;

		if (time != -1) {
			registerTimed(game, events);
		} else {
			registerImmediate(game, events);
		}
	}

	private void registerTimed(IGamePhase game, EventRegistrar events) {
		Collection<BlockBox> regions = this.regions;
		if (regions == null) {
			throw new GameException(new TextComponent("Regions not specified for block set behavior with a set time!"));
		}

		events.listen(GamePhaseEvents.TICK, () -> {
			if (time != game.ticks()) return;

			for (BlockBox region : regions) {
				setInRegion(game, region);
			}
		});
	}

	private void registerImmediate(IGamePhase game, EventRegistrar events) {
		if (regions != null) {
			Long2ObjectMap<List<BlockBox>> regionsByChunk = collectRegionsByChunk(regions);

			events.listen(GameWorldEvents.CHUNK_LOAD, (chunk) -> {
				List<BlockBox> regions = regionsByChunk.remove(chunk.getPos().toLong());
				if (regions == null) {
					return;
				}

				for (BlockBox region : regions) {
					setInRegion(game, region);
				}
			});
		} else {
			events.listen(GameWorldEvents.CHUNK_LOAD, (chunk) -> {
				BlockBox region = BlockBox.ofChunk(chunk.getPos());
				setInRegion(game, region);
			});
		}
	}

	private static Long2ObjectMap<List<BlockBox>> collectRegionsByChunk(Collection<BlockBox> regions) {
		Long2ObjectMap<List<BlockBox>> regionsByChunk = new Long2ObjectOpenHashMap<>();

		for (BlockBox region : regions) {
			LongIterator chunkIterator = region.asChunks().iterator();
			while (chunkIterator.hasNext()) {
				long chunkPos = chunkIterator.nextLong();
				BlockBox chunkRegion = BlockBox.ofChunk(ChunkPos.getX(chunkPos), ChunkPos.getZ(chunkPos));

				BlockBox intersectionRegion = region.intersection(chunkRegion);
				if (intersectionRegion != null) {
					regionsByChunk.computeIfAbsent(chunkPos, l -> new ArrayList<>()).add(intersectionRegion);
				}
			}
		}

		return regionsByChunk;
	}

	private void setInRegion(IGamePhase game, BlockBox region) {
		ServerLevel world = game.getWorld();
		BlockPredicate replace = this.replace;
		BlockStateProvider set = this.set;
		Random random = world.random;

		this.loadRegionChunks(region, world);

		int flags = Constants.BlockFlags.DEFAULT;
		if (!notifyNeighbors) {
			// the constant name is inverted
			flags |= Constants.BlockFlags.UPDATE_NEIGHBORS;
		}

		for (BlockPos pos : region) {
			if (replace == null || replace.matches(world, pos)) {
				BlockState state = set.getState(random, pos);
				world.setBlock(pos, state, flags);
			}
		}
	}

	private void loadRegionChunks(BlockBox region, ServerLevel world) {
		LongSet chunks = region.asChunks();
		LongIterator chunkIterator = chunks.iterator();
		while (chunkIterator.hasNext()) {
			long chunkPos = chunkIterator.nextLong();
			world.getChunk(ChunkPos.getX(chunkPos), ChunkPos.getZ(chunkPos));
		}
	}
}
