package com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.*;

public final class PlantMap implements Iterable<Plant> {
	private final List<Plant> plants = new ArrayList<>();
	private final Map<PlantType, List<Plant>> plantsByType = new Object2ObjectOpenHashMap<>();

	private final Long2ObjectMap<Plant> plantByPos = new Long2ObjectOpenHashMap<>();
	private final Long2ObjectMap<Plant> plantByDecorationPos = new Long2ObjectOpenHashMap<>();

	@Nullable
	public Plant addPlant(PlantType type, PlantFamily family, double value, PlantPlacement placement) {
		PlantCoverage functionalCoverage = placement.getFunctionalCoverage();
		if (functionalCoverage == null || !this.canAddPlantAt(functionalCoverage)) {
			return null;
		}

		PlantCoverage decoration = placement.getDecorationCoverage();
		if (decoration != null) {
			decoration = this.removeDecorationIntersection(decoration);
		}

		Plant plant = new Plant(type, functionalCoverage, decoration, family, value);
		this.addPlant(plant);
		return plant;
	}

	public void addPlant(Plant plant) {
		this.plants.add(plant);
		this.plantsByType.computeIfAbsent(plant.type(), t -> new ArrayList<>())
				.add(plant);

		for (BlockPos pos : plant.functionalCoverage()) {
			this.plantByPos.put(pos.toLong(), plant);
			this.plantByDecorationPos.put(pos.toLong(), plant);
		}

		PlantCoverage decoration = plant.decorationCoverage();
		if (decoration != null) {
			for (BlockPos pos : decoration) {
				this.plantByDecorationPos.put(pos.toLong(), plant);
			}
		}
	}

	private PlantCoverage removeDecorationIntersection(PlantCoverage decoration) {
		LongSet intersection = new LongOpenHashSet();
		for (BlockPos pos : decoration) {
			long posKey = pos.toLong();
			if (this.plantByDecorationPos.containsKey(posKey)) {
				intersection.add(posKey);
			}
		}

		if (!intersection.isEmpty()) {
			return decoration.removeIntersection(intersection);
		} else {
			return decoration;
		}
	}

	public boolean removePlant(Plant plant) {
		if (this.plants.remove(plant)) {
			List<Plant> plantsByType = this.plantsByType.get(plant.type());
			if (plantsByType != null) {
				plantsByType.remove(plant);
			}

			for (BlockPos pos : plant.functionalCoverage()) {
				this.plantByPos.remove(pos.toLong(), plant);
				this.plantByDecorationPos.remove(pos.toLong(), plant);
			}

			PlantCoverage decoration = plant.decorationCoverage();
			if (decoration != null) {
				for (BlockPos pos : decoration) {
					this.plantByDecorationPos.remove(pos.toLong(), plant);
				}
			}

			return true;
		} else {
			return false;
		}
	}

	@Nullable
	public Plant getPlantAt(long pos) {
		return this.plantByDecorationPos.get(pos);
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

	public boolean canAddPlantAt(BlockPos pos) {
		return this.plantByDecorationPos.get(pos.toLong()) == null;
	}

	public boolean canAddPlantAt(PlantCoverage coverage) {
		for (BlockPos pos : coverage) {
			Plant plant = this.plantByDecorationPos.get(pos.toLong());
			if (plant != null) {
				return false;
			}
		}
		return true;
	}

	public List<Plant> getPlantsByType(PlantType type) {
		return this.plantsByType.getOrDefault(type, Collections.emptyList());
	}

	@Override
	public Iterator<Plant> iterator() {
		return this.plants.iterator();
	}
}
