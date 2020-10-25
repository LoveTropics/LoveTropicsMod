package com.lovetropics.minigames.common.minigames.behaviours.instances.trash_dive;

import com.lovetropics.minigames.common.block.LoveTropicsBlocks;
import com.lovetropics.minigames.common.block.TrashBlock;
import com.lovetropics.minigames.common.block.TrashType;
import com.lovetropics.minigames.common.map.MapRegion;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.mojang.datafixers.Dynamic;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.block.Blocks;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class PlaceTrashBehavior implements IMinigameBehavior {
	private static final Logger LOGGER = LogManager.getLogger(PlaceTrashBehavior.class);

	private final TrashType[] trashTypes = TrashType.values();

	private final int count;
	private final String region;

	private final LongSet trashBlocks = new LongOpenHashSet();

	public PlaceTrashBehavior(int count, String region) {
		this.count = count;
		this.region = region;
	}

	public static <T> PlaceTrashBehavior parse(Dynamic<T> root) {
		int count = root.get("count").asInt(10);
		String region = root.get("region").asString("");
		return new PlaceTrashBehavior(count, region);
	}

	@Override
	public void onConstruct(IMinigameInstance minigame) {
		List<MapRegion> regions = new ArrayList<>(minigame.getMapRegions().get(region));
		if (regions.isEmpty()) {
			LOGGER.warn("No matching regions for placing trash! Was given '{}'", region);
			return;
		}

		ServerWorld world = minigame.getWorld();
		Random random = world.rand;

		long totalVolume = 0;
		for (MapRegion region : regions) {
			totalVolume += region.getVolume();
		}

		// place trash weighted by the volumes of each region
		int remaining = count;
		for (MapRegion region : regions) {
			long volume = region.getVolume();
			int amount = (int) (count * totalVolume / volume);

			int i = 0;
			while (i < amount) {
				if (tryPlaceTrash(world, region)) {
					i++;
				}
			}

			remaining -= amount;
		}

		// we're doing integer division: place the remainder randomly
		while (remaining > 0) {
			MapRegion region = regions.get(random.nextInt(regions.size()));
			if (tryPlaceTrash(world, region)) {
				remaining--;
			}
		}
	}

	private boolean tryPlaceTrash(ServerWorld world, MapRegion region) {
		Random random = world.rand;
		BlockPos pos = region.sample(random);

		if (world.getBlockState(pos).getBlock() == Blocks.WATER) {
			TrashType trashType = trashTypes[random.nextInt(trashTypes.length)];
			world.setBlockState(pos, LoveTropicsBlocks.TRASH.get(trashType).getDefaultState()
					.with(TrashBlock.WATERLOGGED, true)
					.with(TrashBlock.FACING, Direction.byHorizontalIndex(random.nextInt(4)))
			);

			trashBlocks.add(pos.toLong());
			return true;
		}

		return false;
	}

	public LongSet getTrashBlocks() {
		return trashBlocks;
	}
}
