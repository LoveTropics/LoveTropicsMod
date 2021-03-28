package com.lovetropics.minigames.common.map.workspace;

import com.lovetropics.minigames.common.map.MapMetadata;
import com.lovetropics.minigames.common.map.MapWorldSettings;
import com.lovetropics.minigames.common.map.generator.ConfiguredGenerator;
import com.lovetropics.minigames.common.map.generator.ConfiguredGenerators;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public final class MapWorkspace {
	private final String id;
	private final RegistryKey<World> dimension;
	private final ConfiguredGenerator generator;

	private final MapWorldSettings worldSettings;

	private final WorkspaceRegions regions;

	MapWorkspace(String id, RegistryKey<World> dimension, ConfiguredGenerator generator, MapWorldSettings worldSettings) {
		this(id, dimension, generator, worldSettings, new WorkspaceRegions(dimension));
	}

	MapWorkspace(String id, RegistryKey<World> dimension, ConfiguredGenerator generator, MapWorldSettings worldSettings, WorkspaceRegions regions) {
		this.id = id;
		this.dimension = dimension;
		this.generator = generator;
		this.worldSettings = worldSettings;
		this.regions = regions;
	}

	public String getId() {
		return id;
	}

	public RegistryKey<World> getDimension() {
		return dimension;
	}

	public ConfiguredGenerator getGenerator() {
		return generator;
	}

	public WorkspaceRegions getRegions() {
		return regions;
	}

	public MapWorldSettings getWorldSettings() {
		return worldSettings;
	}

	public void write(CompoundNBT root) {
		root.put("regions", regions.write(new CompoundNBT()));
		root.put("settings", worldSettings.write(new CompoundNBT()));
		root.putString("generator", generator.getId().toString());
	}

	public static MapWorkspace read(String id, RegistryKey<World> dimension, CompoundNBT root) {
		WorkspaceRegions regions = new WorkspaceRegions(dimension);
		regions.read(root.getCompound("regions"));

		MapWorldSettings worldSettings = new MapWorldSettings();
		worldSettings.read(root.getCompound("settings"));

		ResourceLocation generatorId = new ResourceLocation(root.getString("generator"));
		ConfiguredGenerator generator = ConfiguredGenerators.get(generatorId);
		if (generator == null) {
			generator = ConfiguredGenerators.VOID;
		}

		return new MapWorkspace(id, dimension, generator, worldSettings, regions);
	}

	public void importFrom(MapMetadata metadata) {
		regions.importFrom(metadata.regions);
		worldSettings.importFrom(metadata.settings);
	}
}
