package com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public final class PlantPlacement {
	private PlantCoverage functionalCoverage;
	private PlantCoverage decorationCoverage;
	private Predicate<ServerWorld> place;

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

	public PlantPlacement places(Predicate<ServerWorld> place) {
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

	public boolean place(ServerWorld world) {
		if (this.place == null) {
			return false;
		}
		return this.place.test(world);
	}
}
