package com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;

public final class PlantPlacement {
	@Nullable
	private PlantCoverage functionalCoverage;
	@Nullable
	private PlantCoverage decorationCoverage;
	@Nullable
	private Place place;

	public PlantPlacement covers(BlockPos pos) {
		return covers(PlantCoverage.of(pos));
	}

	public PlantPlacement covers(PlantCoverage coverage) {
		functionalCoverage = coverage;
		return this;
	}

	public PlantPlacement decorationCovers(PlantCoverage coverage) {
		decorationCoverage = coverage;
		return this;
	}

	public PlantPlacement places(Place place) {
		this.place = place;
		return this;
	}

	@Nullable
	public PlantCoverage getFunctionalCoverage() {
		return functionalCoverage;
	}

	@Nullable
	public PlantCoverage getDecorationCoverage() {
		return decorationCoverage;
	}

	public boolean place(ServerLevel world, PlantCoverage coverage) {
		if (place == null) {
			return false;
		}
		return place.place(world, coverage);
	}

	public interface Place {
		boolean place(ServerLevel world, PlantCoverage coverage);
	}
}
