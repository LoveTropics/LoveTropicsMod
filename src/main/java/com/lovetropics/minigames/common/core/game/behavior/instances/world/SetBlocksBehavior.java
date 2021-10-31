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
				MoreCodecs.arrayOrUnit(Codec.STRING, String[]::new).optionalFieldOf("region", new String[0]).forGetter(c -> c.regionKeys),
				Codec.LONG.optionalFieldOf("time").forGetter(c -> c.time != -1 ? Optional.of(c.time) : Optional.empty())
		).apply(instance, SetBlocksBehavior::new);
	});

	private final @Nullable BlockPredicate replace;
	private final BlockStateProvider set;

	private final String[] regionKeys;
	private final long time;

	private Collection<BlockBox> regions;

	private SetBlocksBehavior(Optional<BlockPredicate> replace, BlockStateProvider set, String[] regionKeys, Optional<Long> time) {
		this(replace.orElse(null), set, regionKeys, time.orElse(-1L));
	}

	public SetBlocksBehavior(BlockPredicate replace, BlockStateProvider set, String[] regionKeys, long time) {
		this.replace = replace;
		this.set = set;
		this.regionKeys = regionKeys;
		this.time = time;
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
			throw new GameException(new StringTextComponent("Regions not specified for block set behavior with a set time!"));
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
				List<BlockBox> regions = regionsByChunk.remove(chunk.getPos().asLong());
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
		ServerWorld world = game.getWorld();
		BlockPredicate replace = this.replace;
		BlockStateProvider set = this.set;
		Random random = world.rand;

		this.loadRegionChunks(region, world);

		for (BlockPos pos : region) {
			if (replace == null || replace.test(world, pos)) {
				BlockState state = set.getBlockState(random, pos);
				world.setBlockState(pos, state);
			}
		}
	}

	private void loadRegionChunks(BlockBox region, ServerWorld world) {
		LongSet chunks = region.asChunks();
		LongIterator chunkIterator = chunks.iterator();
		while (chunkIterator.hasNext()) {
			long chunkPos = chunkIterator.nextLong();
			world.getChunk(ChunkPos.getX(chunkPos), ChunkPos.getZ(chunkPos));
		}
	}
}
