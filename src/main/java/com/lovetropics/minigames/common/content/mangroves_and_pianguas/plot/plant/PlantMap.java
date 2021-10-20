package com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.plant;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.*;

public final class PlantMap implements Iterable<Plant> {
	private final List<Plant> plants = new ArrayList<>();
	private final Map<PlantType, List<Plant>> plantsByType = new Object2ObjectOpenHashMap<>();

	private final Long2ObjectMap<Plant> plantByPos = new Long2ObjectOpenHashMap<>();

	public Plant addPlant(PlantType type, PlantCoverage coverage) {
		Plant plant = new Plant(type, coverage);
		this.addPlant(plant);
		return plant;
	}

	public void addPlant(Plant plant) {
		this.addPlantToList(plant);
		this.assignPlantCoverage(plant);
	}

	private void assignPlantCoverage(Plant plant) {
		Set<Plant> intersectingPlants = new ReferenceOpenHashSet<>();
		for (BlockPos pos : plant.coverage()) {
			Plant intersecting = this.plantByPos.put(pos.toLong(), plant);
			if (intersecting != null) {
				intersectingPlants.add(intersecting);
			}
		}

		this.removeIntersectingPlants(plant, intersectingPlants);
	}

	private void removeIntersectingPlants(Plant plant, Set<Plant> intersectingPlants) {
		for (Plant intersecting : intersectingPlants) {
			this.removePlantFromList(intersecting);

			Plant newIntersecting = intersecting.removeIntersection(plant);
			if (newIntersecting != null) {
				this.addPlantToList(newIntersecting);
			}
		}
	}

	public boolean removePlant(Plant plant) {
		if (this.removePlantFromList(plant)) {
			for (BlockPos pos : plant.coverage()) {
				this.plantByPos.remove(pos.toLong(), plant);
			}
			return true;
		} else {
			return false;
		}
	}

	private void addPlantToList(Plant plant) {
		this.plants.add(plant);
		this.plantsByType.computeIfAbsent(plant.type(), t -> new ArrayList<>())
				.add(plant);
	}

	private boolean removePlantFromList(Plant plant) {
		if (this.plants.remove(plant)) {
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

	public List<Plant> getPlantsByType(PlantType type) {
		return this.plantsByType.getOrDefault(type, Collections.emptyList());
	}

	@Override
	public Iterator<Plant> iterator() {
		return this.plants.iterator();
	}
}
