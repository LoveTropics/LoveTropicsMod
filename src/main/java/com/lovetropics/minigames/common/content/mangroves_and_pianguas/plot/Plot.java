package com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.plant.PlantMap;
import com.lovetropics.minigames.common.core.map.MapRegions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;

public final class Plot {
	public final BlockBox bounds;
	public final BlockBox plantBounds;
	public final BlockBox spawn;
	public final BlockBox shop;
	public final BlockBox mobSpawn;

	public final PlantMap plants = new PlantMap();

	private Plot(BlockBox plantBounds, BlockBox spawn, BlockBox shop, BlockBox mobSpawn) {
		this.bounds = BlockBox.of(
				new BlockPos(plantBounds.min.getX(), 0, plantBounds.min.getZ()),
				new BlockPos(plantBounds.max.getX(), 256, plantBounds.max.getZ())
		);
		this.plantBounds = plantBounds;
		this.spawn = spawn;
		this.shop = shop;
		this.mobSpawn = mobSpawn;
	}

	public static Plot associate(Keys keys, MapRegions regions) {
		return new Plot(
				regions.getOrThrow(keys.plot),
				regions.getOrThrow(keys.spawn),
				regions.getOrThrow(keys.shop),
				regions.getOrThrow(keys.mobSpawn)
		);
	}

	public static final class Keys {
		public static final Codec<Keys> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("plot").forGetter(k -> k.plot),
				Codec.STRING.fieldOf("spawn").forGetter(k -> k.spawn),
				Codec.STRING.fieldOf("shop").forGetter(k -> k.shop),
				Codec.STRING.fieldOf("mob_spawn").forGetter(k -> k.mobSpawn)
		).apply(instance, Keys::new));

		private final String plot;
		private final String spawn;
		private final String shop;
		private final String mobSpawn;

		private Keys(String plot, String spawn, String shop, String mobSpawn) {
			this.plot = plot;
			this.spawn = spawn;
			this.shop = shop;
			this.mobSpawn = mobSpawn;
		}
	}
}
