package com.lovetropics.minigames.common.core.map.workspace;

import com.lovetropics.minigames.common.core.dimension.DimensionUtils;
import com.lovetropics.minigames.common.core.dimension.RuntimeDimensionConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.ServerLevelData;

public record WorkspaceDimensionConfig(Holder<DimensionType> dimensionType, ChunkGenerator generator, long seed) {
	public static final Codec<WorkspaceDimensionConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
			DimensionType.CODEC.fieldOf("dimension_type").forGetter(c -> c.dimensionType),
			ChunkGenerator.CODEC.fieldOf("generator").forGetter(c -> c.generator),
			Codec.LONG.fieldOf("seed").forGetter(c -> c.seed)
	).apply(i, WorkspaceDimensionConfig::new));

	public RuntimeDimensionConfig toRuntimeConfig(MinecraftServer server, ServerLevelData worldInfo) {
		Holder<DimensionType> dimensionType = this.dimensionType;
		if (dimensionType == null) {
			dimensionType = DimensionUtils.overworld(server);
		}

		return new RuntimeDimensionConfig(new LevelStem(dimensionType, this.generator), this.seed, worldInfo);
	}
}
