package com.lovetropics.minigames.common.dimension.biome.minigames;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilder;

public class SurviveTheTide2Biome extends Biome {
	public SurviveTheTide2Biome() {
		super(new Builder()
				.surfaceBuilder(SurfaceBuilder.DEFAULT, SurfaceBuilder.GRASS_DIRT_GRAVEL_CONFIG)
				.precipitation(RainType.NONE)
				.category(Category.OCEAN)
				.depth(-1.6F)
				.scale(0.4F)
                .temperature(2.0F)
                .downfall(0.0F)
				.parent(null)
                .waterColor(SurviveTheTideBiome.WATER_COLOR).waterFogColor(SurviveTheTideBiome.WATER_FOG_COLOR)
		);
	}
}
