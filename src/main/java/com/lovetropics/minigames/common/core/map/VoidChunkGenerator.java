package com.lovetropics.minigames.common.core.map;

import com.google.common.collect.ImmutableList;
import com.lovetropics.minigames.common.util.Util;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public final class VoidChunkGenerator extends ChunkGenerator {
	public static final Codec<VoidChunkGenerator> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Biome.CODEC.stable().fieldOf("biome").forGetter(g -> g.biome)
		).apply(instance, instance.stable(VoidChunkGenerator::new));
	});

	private final Supplier<Biome> biome;

	public VoidChunkGenerator(Supplier<Biome> biome) {
		super(new FixedBiomeSource(biome), new StructureSettings(Optional.empty(), Collections.emptyMap()));
		this.biome = biome;
	}

	public VoidChunkGenerator(Registry<Biome> biomeRegistry) {
		this(biomeRegistry, Biomes.THE_VOID);
	}

	public VoidChunkGenerator(MinecraftServer server) {
		this(server.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY));
	}

	public VoidChunkGenerator(Registry<Biome> biomeRegistry, ResourceKey<Biome> biome) {
		this(() -> biomeRegistry.get(biome));
	}

	public static void register() {
		Registry.register(Registry.CHUNK_GENERATOR, Util.resource("void"), CODEC);
	}

	@Override
	protected Codec<? extends ChunkGenerator> codec() {
		return CODEC;
	}

	@Override
	public void applyCarvers(long seed, BiomeManager biomes, ChunkAccess chunk, GenerationStep.Carving carving) {
	}

	@Nullable
	@Override
	public BlockPos findNearestMapFeature(ServerLevel world, StructureFeature<?> structure, BlockPos pos, int range, boolean undiscovered) {
		return null;
	}

	@Override
	public void applyBiomeDecoration(WorldGenRegion world, StructureFeatureManager strutures) {
	}

	@Override
	public List<MobSpawnSettings.SpawnerData> getMobsAt(Biome biomes, StructureFeatureManager structures, MobCategory entityClassification, BlockPos pos) {
		return ImmutableList.of();
	}

	@Override
	public void createStructures(RegistryAccess dynamicRegistries, StructureFeatureManager structures, ChunkAccess chunk, StructureManager templates, long seed) {
	}

	@Override
	public void createReferences(WorldGenLevel world, StructureFeatureManager structures, ChunkAccess chunk) {
	}

	@Override
	public boolean hasStronghold(ChunkPos pos) {
		return false;
	}

	@Override
	public ChunkGenerator withSeed(long seed) {
		return this;
	}

	@Override
	public void buildSurfaceAndBedrock(WorldGenRegion world, ChunkAccess chunk) {
	}

	@Override
	public void fillFromNoise(LevelAccessor world, StructureFeatureManager structures, ChunkAccess chunk) {
	}

	@Override
	public int getBaseHeight(int x, int z, Heightmap.Types heightmapType) {
		return 0;
	}

	@Override
	public BlockGetter getBaseColumn(int x, int z) {
		return VoidBlockReader.INSTANCE;
	}

	static final class VoidBlockReader implements BlockGetter {
		static final VoidBlockReader INSTANCE = new VoidBlockReader();

		private static final BlockState VOID_BLOCK = Blocks.AIR.defaultBlockState();
		private static final FluidState VOID_FLUID = Fluids.EMPTY.defaultFluidState();

		private VoidBlockReader() {
		}

		@Nullable
		@Override
		public BlockEntity getBlockEntity(BlockPos pos) {
			return null;
		}

		@Override
		public BlockState getBlockState(BlockPos pos) {
			return VOID_BLOCK;
		}

		@Override
		public FluidState getFluidState(BlockPos pos) {
			return VOID_FLUID;
		}
	}
}
