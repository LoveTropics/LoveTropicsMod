package com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.plant;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.*;

public final class PlantMap implements Iterable<Plant> {
	private final List<Plant> plants = new ArrayList<>();

	private final Long2ObjectMap<Plant> plantByPos = new Long2ObjectOpenHashMap<>();
	private final Map<PlantType, List<Plant>> plantsByType = new Object2ObjectOpenHashMap<>();

	@Nullable
	public Plant addPlant(PlantType type, PlantCoverage coverage) {
		Plant plant = new Plant(type, coverage);
		if (this.addPlant(plant)) {
			return plant;
		} else {
			return null;
		}
	}

	public boolean addPlant(Plant plant) {
		for (BlockPos pos : plant.coverage()) {
			if (this.plantByPos.containsKey(pos.toLong())) {
				return false;
			}
		}

		// TODO: test intersection
		this.plants.add(plant);

		this.plantsByType.computeIfAbsent(plant.type(), t -> new ArrayList<>())
				.add(plant);

		for (BlockPos pos : plant.coverage()) {
			this.plantByPos.put(pos.toLong(), plant);
		}

		return true;
	}

	public boolean removePlant(Plant plant) {
		if (this.plants.remove(plant)) {
			for (BlockPos pos : plant.coverage()) {
				this.plantByPos.remove(pos.toLong(), plant);
			}

			List<Plant> plantsByType = this.plantsByType.get(plant.type());
			if (plantsByType != null) {
				plantsByType.remove(plant);
			}

			return true;
		} else {
			return false;
		}
	}

	@Nullable
	public Plant getPlantAt(long pos) {
		return this.plantByPos.get(pos);
	}

	@Nullable
	public Plant getPlantAt(BlockPos pos) {
		return this.getPlantAt(pos.toLong());
	}

	@Nullable
	public Plant getPlantAt(BlockPos pos, PlantType type) {
		Plant plant = this.getPlantAt(pos);
		return plant != null && plant.type().equals(type) ? plant : null;
	}

	public boolean hasPlantAt(BlockPos pos) {
		return this.getPlantAt(pos) != null;
	}

	public Collection<Plant> getPlantsByType(PlantType type) {
		return this.plantsByType.getOrDefault(type, Collections.emptyList());
	}

	@Override
	public Iterator<Plant> iterator() {
		return this.plants.iterator();
	}
}
