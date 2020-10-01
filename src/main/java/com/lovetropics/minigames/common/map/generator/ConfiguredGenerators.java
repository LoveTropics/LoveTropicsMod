package com.lovetropics.minigames.common.map.generator;

import com.lovetropics.minigames.common.Util;
import com.lovetropics.minigames.common.dimension.biome.TropicraftBiomes;
import com.lovetropics.minigames.common.map.VoidChunkGenerator;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.provider.OverworldBiomeProvider;
import net.minecraft.world.biome.provider.OverworldBiomeProviderSettings;
import net.minecraft.world.biome.provider.SingleBiomeProvider;
import net.minecraft.world.biome.provider.SingleBiomeProviderSettings;
import net.minecraft.world.gen.ChunkGeneratorType;
import net.minecraft.world.gen.NetherGenSettings;
import net.minecraft.world.gen.OverworldChunkGenerator;
import net.minecraft.world.gen.OverworldGenSettings;
import net.tropicraft.core.common.dimension.biome.TropicraftBiomeProvider;
import net.tropicraft.core.common.dimension.chunk.TropicraftChunkGenerator;
import net.tropicraft.core.common.dimension.chunk.TropicraftChunkGeneratorTypes;
import net.tropicraft.core.common.dimension.config.TropicraftBiomeProviderSettings;
import net.tropicraft.core.common.dimension.config.TropicraftGeneratorSettings;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class ConfiguredGenerators {
	private static final Map<ResourceLocation, ConfiguredGenerator> REGISTRY = new HashMap<>();

	public static final ConfiguredGenerator OVERWORLD = register(new ConfiguredGenerator(
			new ResourceLocation("overworld"),
			world -> {
				OverworldBiomeProvider biomeProvider = new OverworldBiomeProvider(new OverworldBiomeProviderSettings(world.getWorldInfo()));
				return new OverworldChunkGenerator(world, biomeProvider, new OverworldGenSettings());
			}
	));

	public static final ConfiguredGenerator NETHER = register(new ConfiguredGenerator(
			new ResourceLocation("nether"),
			world -> {
				NetherGenSettings nethergensettings = ChunkGeneratorType.CAVES.createSettings();
				nethergensettings.setDefaultBlock(Blocks.NETHERRACK.getDefaultState());
				nethergensettings.setDefaultFluid(Blocks.LAVA.getDefaultState());
				SingleBiomeProvider biomeProvider = new SingleBiomeProvider(new SingleBiomeProviderSettings(world.getWorldInfo()).setBiome(Biomes.NETHER));
				return ChunkGeneratorType.CAVES.create(world, biomeProvider, nethergensettings);
			}
	));

	public static final ConfiguredGenerator TROPICS = register(new ConfiguredGenerator(
			new ResourceLocation("tropicraft", "tropics"),
			world -> {
				ChunkGeneratorType<TropicraftGeneratorSettings, TropicraftChunkGenerator> type = TropicraftChunkGeneratorTypes.TROPICS.get();
				TropicraftGeneratorSettings settings = type.createSettings();
				TropicraftBiomeProviderSettings biomeSettings = new TropicraftBiomeProviderSettings(world.getWorldInfo()).setGeneratorSettings(settings);
				return type.create(world, new TropicraftBiomeProvider(biomeSettings), settings);
			}
	));

	public static final ConfiguredGenerator SURVIVE_THE_TIDE = register(new ConfiguredGenerator(
			Util.resource("survive_the_tide"),
			world -> {
				ChunkGeneratorType<TropicraftGeneratorSettings, TropicraftChunkGenerator> type = TropicraftChunkGeneratorTypes.TROPICS.get();
				TropicraftGeneratorSettings settings = type.createSettings();
				SingleBiomeProviderSettings biomeSettings = new SingleBiomeProviderSettings(world.getWorldInfo()).setBiome(TropicraftBiomes.SURVIVE_THE_TIDE.get());
				return type.create(world, new SingleBiomeProvider(biomeSettings), settings);
			}
	));

	public static final ConfiguredGenerator VOID = register(new ConfiguredGenerator(
			Util.resource("void"),
			VoidChunkGenerator::new
	));

	public static ConfiguredGenerator register(ConfiguredGenerator generator) {
		REGISTRY.put(generator.getId(), generator);
		return generator;
	}

	@Nullable
	public static ConfiguredGenerator get(ResourceLocation key) {
		return REGISTRY.get(key);
	}

	public static Collection<ResourceLocation> getKeys() {
		return REGISTRY.keySet();
	}
}
