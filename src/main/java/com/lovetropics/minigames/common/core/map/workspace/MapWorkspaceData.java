package com.lovetropics.minigames.common.core.map.workspace;

import com.lovetropics.minigames.common.core.dimension.DimensionUtils;
import com.lovetropics.minigames.common.core.dimension.RuntimeDimensionHandle;
import com.lovetropics.minigames.common.core.map.MapRegions;
import com.lovetropics.minigames.common.core.map.MapWorldSettings;
import com.lovetropics.minigames.common.core.map.VoidChunkGenerator;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nullable;
import java.util.Optional;

public final class MapWorkspaceData {
	public static final Codec<MapWorkspaceData> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.STRING.fieldOf("id").forGetter(c -> c.id),
				WorkspaceDimensionConfig.CODEC.optionalFieldOf("dimension").forGetter(c -> Optional.ofNullable(c.dimension)),
				MapWorldSettings.CODEC.fieldOf("settings").forGetter(c -> c.worldSettings),
				MapRegions.CODEC.fieldOf("regions").forGetter(c -> c.regions)
		).apply(instance, MapWorkspaceData::new);
	});

	public final String id;
	public final @Nullable WorkspaceDimensionConfig dimension;
	public final MapWorldSettings worldSettings;
	public final MapRegions regions;

	public MapWorkspaceData(String id, Optional<WorkspaceDimensionConfig> dimension, MapWorldSettings worldSettings, MapRegions regions) {
		this.id = id;
		this.dimension = dimension.orElse(null);
		this.worldSettings = worldSettings;
		this.regions = regions;
	}

	public MapWorkspace create(MinecraftServer server, RuntimeDimensionHandle dimensionHandle) {
		WorkspaceDimensionConfig dimension = this.dimension;
		if (dimension == null) {
			dimension = new WorkspaceDimensionConfig(DimensionUtils.overworld(server), new VoidChunkGenerator(server), 0);
		}

		WorkspaceRegions regions = new WorkspaceRegions(dimensionHandle.asKey());
		regions.importFrom(this.regions);

		return new MapWorkspace(id, dimension, worldSettings, regions, dimensionHandle);
	}
}
