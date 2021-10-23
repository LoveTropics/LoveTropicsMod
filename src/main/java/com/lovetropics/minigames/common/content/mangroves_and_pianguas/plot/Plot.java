package com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.plant.PlantMap;
import com.lovetropics.minigames.common.core.map.MapRegions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public final class Plot {
	public final BlockBox bounds;
	public final BlockBox plantBounds;
	public final BlockBox spawn;
	public final BlockBox shop;
	public final BlockBox mobSpawn;

	public final Direction forward;
	public final Direction shopForward;
	public final Direction spawnForward;

	public final PlantMap plants = new PlantMap();

	private Plot(
			BlockBox bounds, BlockBox plantBounds,
			BlockBox spawn, BlockBox shop,
			BlockBox mobSpawn,
			Direction forward, Direction shopForward, Direction spawnForward
	) {
		this.bounds = bounds;
		this.plantBounds = plantBounds;
		this.spawn = spawn;
		this.shop = shop;
		this.mobSpawn = mobSpawn;
		this.forward = forward;
		this.shopForward = shopForward;
		this.spawnForward = spawnForward;
	}

	public static Plot associate(Config config, MapRegions regions) {
		BlockBox plantBounds = regions.getOrThrow(config.plot);
		BlockBox bounds = BlockBox.of(
				new BlockPos(plantBounds.min.getX(), 0, plantBounds.min.getZ()),
				new BlockPos(plantBounds.max.getX(), 256, plantBounds.max.getZ())
		);
		BlockBox spawn = regions.getOrThrow(config.spawn);
		BlockBox shop = regions.getOrThrow(config.shop);
		BlockBox mobSpawn = regions.getOrThrow(config.mobSpawn);

		Direction forward = getDirectionBetween(bounds, mobSpawn);
		Direction shopForward = getDirectionBetween(shop, bounds);
		Direction spawnForward = getDirectionBetween(spawn, bounds);

		return new Plot(
				bounds, plantBounds,
				spawn, shop,
				mobSpawn,
				forward, shopForward, spawnForward
		);
	}

	private static Direction getDirectionBetween(BlockBox from, BlockBox to) {
		BlockPos fromCenter = from.getCenterBlock();
		BlockPos toCenter = to.getCenterBlock();
		int dx = toCenter.getX() - fromCenter.getX();
		int dz = toCenter.getZ() - fromCenter.getZ();
		if (Math.abs(dx) > Math.abs(dz)) {
			return dx > 0 ? Direction.EAST : Direction.WEST;
		} else {
			return dz > 0 ? Direction.SOUTH : Direction.NORTH;
		}
	}

	public static final class Config {
		public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("plot").forGetter(c -> c.plot),
				Codec.STRING.fieldOf("spawn").forGetter(c -> c.spawn),
				Codec.STRING.fieldOf("shop").forGetter(c -> c.shop),
				Codec.STRING.fieldOf("mob_spawn").forGetter(c -> c.mobSpawn)
		).apply(instance, Config::new));

		private final String plot;
		private final String spawn;
		private final String shop;
		private final String mobSpawn;

		private Config(String plot, String spawn, String shop, String mobSpawn) {
			this.plot = plot;
			this.spawn = spawn;
			this.shop = shop;
			this.mobSpawn = mobSpawn;
		}
	}
}
