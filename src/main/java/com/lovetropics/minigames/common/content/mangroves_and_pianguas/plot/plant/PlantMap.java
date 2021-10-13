package com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.plant;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.*;

public final class PlantMap implements Iterable<Plant<?>> {
	private final List<Plant<?>> plants = new ArrayList<>();

	private final Long2ObjectMap<Plant<?>> plantByPos = new Long2ObjectOpenHashMap<>();
	private final Reference2ObjectMap<PlantType<?>, List<Plant<?>>> plantsByType = new Reference2ObjectOpenHashMap<>();

	public <S> void addPlant(PlantType<S> type, PlantCoverage coverage, S state) {
		this.addPlant(Plant.create(type, coverage, state));
	}

	public void addPlant(PlantType<Unit> type, PlantCoverage coverage) {
		this.addPlant(Plant.create(type, coverage));
	}

	public <S> void addPlant(Plant<S> plant) {
		// TODO: test intersection
		this.plants.add(plant);

		this.plantsByType.computeIfAbsent(plant.type(), t -> new ArrayList<>())
				.add(plant);

		for (BlockPos pos : plant.coverage()) {
			this.plantByPos.put(pos.toLong(), plant);
		}
	}

	public boolean removePlant(Plant<?> plant) {
		if (this.plants.remove(plant)) {
			for (BlockPos pos : plant.coverage()) {
				this.plantByPos.remove(pos.toLong(), plant);
			}

			List<Plant<?>> plantsByType = this.plantsByType.get(plant.type());
			if (plantsByType != null) {
				plantsByType.remove(plant);
			}

			return true;
		} else {
			return false;
		}
	}

	@Nullable
	public Plant<?> getPlantAt(long pos) {
		return this.plantByPos.get(pos);
	}

	@Nullable
	public Plant<?> getPlantAt(BlockPos pos) {
		return this.getPlantAt(pos.toLong());
	}

	public boolean hasPlantAt(BlockPos pos) {
		return this.getPlantAt(pos) != null;
	}

	@SuppressWarnings("unchecked")
	public <S> Collection<Plant<S>> getPlantsByType(PlantType<S> type) {
		List<Plant<?>> plants = this.plantsByType.getOrDefault(type, Collections.emptyList());
		return (List<Plant<S>>) (List<?>) plants;
	}

	@Override
	public Iterator<Plant<?>> iterator() {
		return this.plants.iterator();
	}
}
