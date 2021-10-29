package com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;

public final class PlantPlacement {
	private PlantCoverage functionalCoverage;
	private PlantCoverage decorationCoverage;
	private Place place;

	public PlantPlacement covers(BlockPos pos) {
		return this.covers(PlantCoverage.of(pos));
	}

	public PlantPlacement covers(PlantCoverage coverage) {
		this.functionalCoverage = coverage;
		return this;
	}

	public PlantPlacement decorationCovers(PlantCoverage coverage) {
		this.decorationCoverage = coverage;
		return this;
	}

	public PlantPlacement places(Place place) {
		this.place = place;
		return this;
	}

	@Nullable
	public PlantCoverage getFunctionalCoverage() {
		return this.functionalCoverage;
	}

	@Nullable
	public PlantCoverage getDecorationCoverage() {
		return this.decorationCoverage;
	}

	public boolean place(ServerWorld world, PlantCoverage coverage) {
		if (this.place == null) {
			return false;
		}
		return this.place.place(world, coverage);
	}

	public interface Place {
		boolean place(ServerWorld world, PlantCoverage coverage);
	}
}
