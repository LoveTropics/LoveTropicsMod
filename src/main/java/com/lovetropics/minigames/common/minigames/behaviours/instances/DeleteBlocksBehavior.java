package com.lovetropics.minigames.common.minigames.behaviours.instances;

import com.lovetropics.minigames.common.map.MapRegion;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.util.Collection;

// TODO: make more complex to allow replacing blocks
public final class DeleteBlocksBehavior implements IMinigameBehavior {
	public static final Codec<DeleteBlocksBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.STRING.fieldOf("region").forGetter(c -> c.regionKey),
				Codec.LONG.fieldOf("time").forGetter(c -> c.time)
		).apply(instance, DeleteBlocksBehavior::new);
	});

	private final String regionKey;
	private final long time;

	private Collection<MapRegion> regions;

	public DeleteBlocksBehavior(String regionKey, long time) {
		this.regionKey = regionKey;
		this.time = time;
	}

	@Override
	public void onConstruct(IMinigameInstance minigame) {
		regions = minigame.getMapRegions().get(regionKey);
	}

	@Override
	public void worldUpdate(IMinigameInstance minigame, ServerWorld world) {
		if (time == minigame.ticks()) {
			for (MapRegion region : regions) {
				for (BlockPos pos : region) {
					world.setBlockState(pos, Blocks.AIR.getDefaultState());
				}
			}
		}
	}
}
