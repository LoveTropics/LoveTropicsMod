package com.lovetropics.minigames.common.content.biodiversity_blitz.plot;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.PlotWalls;
import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.PlotWaveState;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.PlantMap;
import com.lovetropics.minigames.common.core.game.map.RegionPattern;
import com.lovetropics.minigames.common.core.map.MapRegions;
import com.lovetropics.minigames.common.util.Util;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelHeightAccessor;

import javax.annotation.Nullable;

public final class Plot {
	public final BlockBox bounds;
	public final BlockBox plantBounds;
	public final BlockBox floorBounds;
	public final BlockBox spawn;
	public final BlockBox shop;
	public final BlockBox plantShop;
	public final BlockBox mobShop;
	public final BlockBox mobSpawn;

	public final Direction forward;
	public final Direction spawnForward;

	public final PlantMap plants = new PlantMap();
	public final PlotWalls walls;

	public int nextCurrencyIncrement;
	public final PlotWaveState waveState = new PlotWaveState();

	private Plot(
			BlockBox bounds, BlockBox plantBounds, BlockBox floorBounds,
			BlockBox spawn, BlockBox shop, BlockBox plantShop, BlockBox mobShop,
			BlockBox mobSpawn,
			Direction forward, Direction spawnForward
	) {
		this.bounds = bounds;
		this.plantBounds = plantBounds;
		this.floorBounds = floorBounds;
		this.spawn = spawn;
		this.shop = shop;
		this.plantShop = plantShop;
		this.mobShop = mobShop;
		this.mobSpawn = mobSpawn;
		this.forward = forward;
		this.spawnForward = spawnForward;

		this.walls = new PlotWalls(this.bounds.asAabb().minmax(this.mobSpawn.asAabb()));
	}

	public static Plot create(LevelHeightAccessor level, Config config, RegionKeys regionKeys, MapRegions regions) {
		BlockBox plantBounds = regionKeys.plot.getOrThrow(regions, config.key);
		BlockBox bounds = BlockBox.of(
				new BlockPos(plantBounds.min().getX(), level.getMinBuildHeight(), plantBounds.min().getZ()),
				new BlockPos(plantBounds.max().getX(), level.getMaxBuildHeight(), plantBounds.max().getZ())
		);
		BlockBox floorBounds = BlockBox.of(
				new BlockPos(plantBounds.min().getX(), plantBounds.min().getY() - 1, plantBounds.min().getZ()),
				new BlockPos(plantBounds.max().getX(), plantBounds.min().getY() - 1, plantBounds.max().getZ())
		);
		BlockBox spawn = regionKeys.spawn.getOrThrow(regions, config.key);
		BlockBox shop = regionKeys.shop.getOrThrow(regions, config.key);
		BlockBox plantShop = regionKeys.plantShop.getOrThrow(regions, config.key);
		BlockBox mobShop = regionKeys.mobShop.getOrThrow(regions, config.key);
		BlockBox mobSpawn = regionKeys.mobSpawn.getOrThrow(regions, config.key);

		// TODO: this logic doesn't work for teams
		Direction forward = Util.getDirectionBetween(bounds, mobSpawn);
		Direction spawnForward = Util.getDirectionBetween(spawn, bounds);

		return new Plot(
				bounds, plantBounds, floorBounds,
				spawn, shop, plantShop, mobShop,
				mobSpawn, forward, spawnForward
		);
	}

	@Nullable
	public BlockBox regionByName(String name) {
		// TODO: we need a better generic system for systems of region names that can be referenced
		return switch (name) {
			case "shop" -> this.shop;
			case "plant_shop" -> this.plantShop;
			case "mob_shop" -> this.mobShop;
			default -> null;
		};
	}

	public static final class RegionKeys {
		public static final Codec<RegionKeys> CODEC = RecordCodecBuilder.create(instance -> {
			return instance.group(
					RegionPattern.CODEC.fieldOf("plot").forGetter(c -> c.plot),
					RegionPattern.CODEC.fieldOf("spawn").forGetter(c -> c.spawn),
					RegionPattern.CODEC.fieldOf("shop").forGetter(c -> c.shop),
					RegionPattern.CODEC.fieldOf("plant_shop").forGetter(c -> c.plantShop),
					RegionPattern.CODEC.fieldOf("mob_shop").forGetter(c -> c.mobShop),
					RegionPattern.CODEC.fieldOf("mob_spawn").forGetter(c -> c.mobSpawn)
			).apply(instance, RegionKeys::new);
		});

		private final RegionPattern plot;
		private final RegionPattern spawn;
		private final RegionPattern shop;
		private final RegionPattern plantShop;
		private final RegionPattern mobShop;
		private final RegionPattern mobSpawn;

		private RegionKeys(RegionPattern plot, RegionPattern spawn, RegionPattern shop, RegionPattern plantShop, RegionPattern mobShop, RegionPattern mobSpawn) {
			this.plot = plot;
			this.spawn = spawn;
			this.shop = shop;
			this.plantShop = plantShop;
			this.mobShop = mobShop;
			this.mobSpawn = mobSpawn;
		}
	}

	public static final class Config {
		public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("key").forGetter(c -> c.key)
		).apply(instance, Config::new));

		private final String key;

		private Config(String key) {
			this.key = key;
		}
	}
}
