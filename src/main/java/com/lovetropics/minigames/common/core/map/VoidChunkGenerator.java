package com.lovetropics.minigames.common.core.map;

import com.google.common.collect.ImmutableList;
import com.lovetropics.minigames.common.util.Util;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityClassification;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraft.world.biome.provider.SingleBiomeProvider;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;
import net.minecraft.world.server.ServerWorld;

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
		super(new SingleBiomeProvider(biome), new DimensionStructuresSettings(Optional.empty(), Collections.emptyMap()));
		this.biome = biome;
	}

	public VoidChunkGenerator(Registry<Biome> biomeRegistry) {
		this(biomeRegistry, Biomes.THE_VOID);
	}

	public VoidChunkGenerator(MinecraftServer server) {
		this(server.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY));
	}

	public VoidChunkGenerator(Registry<Biome> biomeRegistry, RegistryKey<Biome> biome) {
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
	public void applyCarvers(long seed, BiomeManager biomes, IChunk chunk, GenerationStage.Carving carving) {
	}

	@Nullable
	@Override
	public BlockPos findNearestMapFeature(ServerWorld world, Structure<?> structure, BlockPos pos, int range, boolean undiscovered) {
		return null;
	}

	@Override
	public void applyBiomeDecoration(WorldGenRegion world, StructureManager strutures) {
	}

	@Override
	public List<MobSpawnInfo.Spawners> getMobsAt(Biome biomes, StructureManager structures, EntityClassification entityClassification, BlockPos pos) {
		return ImmutableList.of();
	}

	@Override
	public void createStructures(DynamicRegistries dynamicRegistries, StructureManager structures, IChunk chunk, TemplateManager templates, long seed) {
	}

	@Override
	public void createReferences(ISeedReader world, StructureManager structures, IChunk chunk) {
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
	public void buildSurfaceAndBedrock(WorldGenRegion world, IChunk chunk) {
	}

	@Override
	public void fillFromNoise(IWorld world, StructureManager structures, IChunk chunk) {
	}

	@Override
	public int getBaseHeight(int x, int z, Heightmap.Type heightmapType) {
		return 0;
	}

	@Override
	public IBlockReader getBaseColumn(int x, int z) {
		return VoidBlockReader.INSTANCE;
	}

	static final class VoidBlockReader implements IBlockReader {
		static final VoidBlockReader INSTANCE = new VoidBlockReader();

		private static final BlockState VOID_BLOCK = Blocks.AIR.defaultBlockState();
		private static final FluidState VOID_FLUID = Fluids.EMPTY.defaultFluidState();

		private VoidBlockReader() {
		}

		@Nullable
		@Override
		public TileEntity getBlockEntity(BlockPos pos) {
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
