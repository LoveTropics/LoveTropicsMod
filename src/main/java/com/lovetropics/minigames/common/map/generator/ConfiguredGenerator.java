package com.lovetropics.minigames.common.map.generator;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.gen.ChunkGenerator;

public final class ConfiguredGenerator {
	private final ResourceLocation id;
	private final Factory factory;

	public ConfiguredGenerator(ResourceLocation id, Factory factory) {
		this.id = id;
		this.factory = factory;
	}

	public ResourceLocation getId() {
		return id;
	}

	public ChunkGenerator<?> createGenerator(World world) {
		return this.factory.createGenerator(world);
	}

	public interface Factory {
		ChunkGenerator<?> createGenerator(World world);
	}
}
