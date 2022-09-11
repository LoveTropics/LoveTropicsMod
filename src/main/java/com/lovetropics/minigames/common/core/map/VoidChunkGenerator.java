package com.lovetropics.minigames.common.core.map;

import com.lovetropics.minigames.common.util.Util;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionDefaults;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class VoidChunkGenerator extends ChunkGenerator {
	public static final Codec<VoidChunkGenerator> CODEC = RecordCodecBuilder.create(i -> commonCodec(i).and(i.group(
			Biome.CODEC.stable().fieldOf("biome").forGetter(g -> g.biome)
	).t1()).apply(i, i.stable(VoidChunkGenerator::new)));

	private final Holder<Biome> biome;

	public VoidChunkGenerator(final Registry<StructureSet> structureSets, final Holder<Biome> biome) {
		super(structureSets, Optional.empty(), new FixedBiomeSource(biome));
		this.biome = biome;
	}

	public VoidChunkGenerator(Registry<StructureSet> structureSets, Registry<Biome> biomeRegistry) {
		this(structureSets, biomeRegistry, Biomes.THE_VOID);
	}

	public VoidChunkGenerator(MinecraftServer server) {
		this(server.registryAccess().registryOrThrow(Registry.STRUCTURE_SET_REGISTRY), server.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY));
	}

	public VoidChunkGenerator(Registry<StructureSet> structureSets, Registry<Biome> biomeRegistry, ResourceKey<Biome> biome) {
		this(structureSets, biomeRegistry.getOrCreateHolder(biome));
	}

	public static void register() {
		Registry.register(Registry.CHUNK_GENERATOR, Util.resource("void"), CODEC);
	}

	@Override
	protected Codec<? extends ChunkGenerator> codec() {
		return CODEC;
	}

	@Override
	public void createStructures(RegistryAccess dynamicRegistries, StructureFeatureManager structures, ChunkAccess chunk, StructureManager templates, long seed) {
	}

	@Override
	public void createReferences(WorldGenLevel world, StructureFeatureManager structures, ChunkAccess chunk) {
	}

	@Override
	public CompletableFuture<ChunkAccess> fillFromNoise(final Executor executor, final Blender blender, final StructureFeatureManager structures, final ChunkAccess chunk) {
		return CompletableFuture.completedFuture(chunk);
	}

	@Override
	public int getSeaLevel() {
		return 0;
	}

	@Override
	public int getMinY() {
		return 0;
	}

	@Override
	public int getBaseHeight(final int x, final int z, final Heightmap.Types heightmap, final LevelHeightAccessor level) {
		return level.getMinBuildHeight();
	}

	@Override
	public NoiseColumn getBaseColumn(final int x, final int z, final LevelHeightAccessor level) {
		final BlockState[] blocks = new BlockState[level.getMaxBuildHeight()];
		Arrays.fill(blocks, Blocks.AIR.defaultBlockState());
		return new NoiseColumn(level.getMinBuildHeight(), blocks);
	}

	@Override
	public void addDebugScreenInfo(final List<String> info, final BlockPos pos) {
	}

	@Override
	public ChunkGenerator withSeed(long seed) {
		return this;
	}

	@Override
	public Climate.Sampler climateSampler() {
		return Climate.empty();
	}

	@Override
	public void applyCarvers(final WorldGenRegion region, final long seed, final BiomeManager biomes, final StructureFeatureManager structures, final ChunkAccess chunk, final GenerationStep.Carving step) {
	}

	@Override
	public void buildSurface(final WorldGenRegion region, final StructureFeatureManager structures, final ChunkAccess chunk) {
	}

	@Override
	public void spawnOriginalMobs(final WorldGenRegion region) {
	}

	@Override
	public int getGenDepth() {
		return DimensionDefaults.OVERWORLD_LEVEL_HEIGHT;
	}
}
