package com.lovetropics.minigames.common.minigames.behaviours.instances;

import com.lovetropics.minigames.common.map.MapRegion;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.mojang.datafixers.Dynamic;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collection;

public final class DeleteBlocksBehavior implements IMinigameBehavior {
	private final String regionKey;
	private final long time;

	private Collection<MapRegion> regions;

	public DeleteBlocksBehavior(String regionKey, long time) {
		this.regionKey = regionKey;
		this.time = time;
	}

	public static <T> DeleteBlocksBehavior parse(Dynamic<T> root) {
		String regionKey = root.get("region").asString("");
		long time = root.get("time").asLong(0);
		return new DeleteBlocksBehavior(regionKey, time);
	}

	@Override
	public void onConstruct(IMinigameInstance minigame) {
		regions = minigame.getMapRegions().get(regionKey);
	}

	@Override
	public void worldUpdate(IMinigameInstance minigame, World world) {
		if (time == minigame.ticks()) {
			for (MapRegion region : regions) {
				for (BlockPos pos : region) {
					world.setBlockState(pos, Blocks.AIR.getDefaultState());
				}
			}
		}
	}
}
