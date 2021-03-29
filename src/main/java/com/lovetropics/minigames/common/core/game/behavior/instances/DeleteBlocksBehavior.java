package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.minigames.common.core.map.MapRegion;
import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.util.Collection;

// TODO: make more complex to allow replacing blocks
public final class DeleteBlocksBehavior implements IGameBehavior {
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
	public void onConstruct(IGameInstance minigame) {
		regions = minigame.getMapRegions().get(regionKey);
	}

	@Override
	public void worldUpdate(IGameInstance minigame, ServerWorld world) {
		if (time == minigame.ticks()) {
			for (MapRegion region : regions) {
				for (BlockPos pos : region) {
					world.setBlockState(pos, Blocks.AIR.getDefaultState());
				}
			}
		}
	}
}
