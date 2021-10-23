package com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.lib.codec.MoreCodecs;
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

	public final PlantMap plants = new PlantMap();

	private Plot(BlockBox plantBounds, BlockBox spawn, BlockBox shop, BlockBox mobSpawn, Direction forward) {
		this.bounds = BlockBox.of(
				new BlockPos(plantBounds.min.getX(), 0, plantBounds.min.getZ()),
				new BlockPos(plantBounds.max.getX(), 256, plantBounds.max.getZ())
		);
		this.plantBounds = plantBounds;
		this.spawn = spawn;
		this.shop = shop;
		this.mobSpawn = mobSpawn;
		this.forward = forward;
	}

	public static Plot associate(Config config, MapRegions regions) {
		return new Plot(
				regions.getOrThrow(config.plot),
				regions.getOrThrow(config.spawn),
				regions.getOrThrow(config.shop),
				regions.getOrThrow(config.mobSpawn),
				config.forward
		);
	}

	public static final class Config {
		private static final Codec<Direction> DIRECTION_CODEC = MoreCodecs.stringVariants(
				Direction.Plane.HORIZONTAL.getDirectionValues().toArray(Direction[]::new),
				Direction::getName2
		);

		public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("plot").forGetter(c -> c.plot),
				Codec.STRING.fieldOf("spawn").forGetter(c -> c.spawn),
				Codec.STRING.fieldOf("shop").forGetter(c -> c.shop),
				Codec.STRING.fieldOf("mob_spawn").forGetter(c -> c.mobSpawn),
				DIRECTION_CODEC.fieldOf("forward").forGetter(c -> c.forward)
		).apply(instance, Config::new));

		private final String plot;
		private final String spawn;
		private final String shop;
		private final String mobSpawn;
		private final Direction forward;

		private Config(String plot, String spawn, String shop, String mobSpawn, Direction forward) {
			this.plot = plot;
			this.spawn = spawn;
			this.shop = shop;
			this.mobSpawn = mobSpawn;
			this.forward = forward;
		}
	}
}
