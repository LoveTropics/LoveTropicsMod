package com.lovetropics.minigames.common.minigames.behaviours.instances;

import com.lovetropics.minigames.common.map.MapRegion;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.mojang.datafixers.Dynamic;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeverBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collection;

public final class ActivateLeversBehavior implements IMinigameBehavior {
	private final String regionKey;
	private final long time;

	private Collection<MapRegion> regions;

	public ActivateLeversBehavior(String regionKey, long time) {
		this.regionKey = regionKey;
		this.time = time;
	}

	public static <T> ActivateLeversBehavior parse(Dynamic<T> root) {
		String regionKey = root.get("region").asString("");
		long time = root.get("time").asLong(0);
		return new ActivateLeversBehavior(regionKey, time);
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
					BlockState state = world.getBlockState(pos);
					if (state.getBlock() instanceof LeverBlock) {
						((LeverBlock) state.getBlock()).func_226939_d_(state, world, pos);
					}
				}
			}
		}
	}
}
