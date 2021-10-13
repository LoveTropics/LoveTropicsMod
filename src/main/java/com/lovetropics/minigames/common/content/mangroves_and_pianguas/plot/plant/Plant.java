package com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.plant;

import net.minecraft.util.Unit;

public final class Plant<S> {
	private final PlantType<S> type;
	private final PlantCoverage coverage;
	private final S state;

	private Plant(PlantType<S> type, PlantCoverage coverage, S state) {
		this.type = type;
		this.coverage = coverage;
		this.state = state;
	}

	public static <S> Plant<S> create(PlantType<S> type, PlantCoverage coverage, S state) {
		return new Plant<>(type, coverage, state);
	}

	public static Plant<Unit> create(PlantType<Unit> type, PlantCoverage coverage) {
		return new Plant<>(type, coverage, Unit.INSTANCE);
	}

	public PlantType<S> type() {
		return type;
	}

	public PlantCoverage coverage() {
		return coverage;
	}

	public S state() {
		return state;
	}
}
