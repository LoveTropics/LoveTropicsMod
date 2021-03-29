package com.lovetropics.minigames.common.core.map.workspace;

import com.lovetropics.minigames.common.core.dimension.RuntimeDimensionHandle;
import com.lovetropics.minigames.common.core.map.MapMetadata;
import com.lovetropics.minigames.common.core.map.MapWorldSettings;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;

import java.util.Optional;

public final class MapWorkspace {
	private final String id;
	private final WorkspaceDimensionConfig dimension;
	private final MapWorldSettings worldSettings;
	private final WorkspaceRegions regions;
	private final RuntimeDimensionHandle dimensionHandle;

	MapWorkspace(String id, WorkspaceDimensionConfig dimension, MapWorldSettings worldSettings, RuntimeDimensionHandle dimensionHandle) {
		this(id, dimension, worldSettings, new WorkspaceRegions(dimensionHandle.asKey()), dimensionHandle);
	}

	MapWorkspace(String id, WorkspaceDimensionConfig dimension, MapWorldSettings worldSettings, WorkspaceRegions regions, RuntimeDimensionHandle dimensionHandle) {
		this.id = id;
		this.dimension = dimension;
		this.worldSettings = worldSettings;
		this.regions = regions;
		this.dimensionHandle = dimensionHandle;
	}

	public String getId() {
		return id;
	}

	public RegistryKey<World> getDimension() {
		return dimensionHandle.asKey();
	}

	public RuntimeDimensionHandle getHandle() {
		return dimensionHandle;
	}

	public WorkspaceDimensionConfig getDimensionConfig() {
		return dimension;
	}

	public WorkspaceRegions getRegions() {
		return regions;
	}

	public MapWorldSettings getWorldSettings() {
		return worldSettings;
	}

	public MapWorkspaceData intoData() {
		return new MapWorkspaceData(id, Optional.of(dimension), worldSettings, regions.compile());
	}

	public void importFrom(MapMetadata metadata) {
		regions.importFrom(metadata.regions);
		worldSettings.importFrom(metadata.settings);
	}
}
