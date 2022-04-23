package com.lovetropics.minigames.common.core.map.workspace;

import com.lovetropics.minigames.common.core.dimension.DimensionUtils;
import com.lovetropics.minigames.common.core.dimension.RuntimeDimensionConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Dimension;
import net.minecraft.world.DimensionType;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.storage.IServerWorldInfo;

import java.util.function.Supplier;

public final class WorkspaceDimensionConfig {
	public static final Codec<WorkspaceDimensionConfig> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				DimensionType.CODEC.fieldOf("dimension_type").forGetter(c -> c.dimensionType),
				ChunkGenerator.CODEC.fieldOf("generator").forGetter(c -> c.generator),
				Codec.LONG.fieldOf("seed").forGetter(c -> c.seed)
		).apply(instance, WorkspaceDimensionConfig::new);
	});

	public final Supplier<DimensionType> dimensionType;
	public final ChunkGenerator generator;
	public final long seed;

	public WorkspaceDimensionConfig(Supplier<DimensionType> dimensionType, ChunkGenerator generator, long seed) {
		this.dimensionType = dimensionType;
		this.generator = generator;
		this.seed = seed;
	}

	public RuntimeDimensionConfig toRuntimeConfig(MinecraftServer server, IServerWorldInfo worldInfo) {
		Supplier<DimensionType> dimensionType = this.dimensionType;
		if (dimensionType == null) {
			dimensionType = DimensionUtils.overworld(server);
		}

		return new RuntimeDimensionConfig(new Dimension(dimensionType, this.generator), this.seed, worldInfo);
	}
}
