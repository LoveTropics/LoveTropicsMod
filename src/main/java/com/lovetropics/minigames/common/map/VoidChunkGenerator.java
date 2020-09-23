package com.lovetropics.minigames.common.map;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.EntityClassification;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.biome.provider.SingleBiomeProvider;
import net.minecraft.world.biome.provider.SingleBiomeProviderSettings;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.ChunkGeneratorType;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.tropicraft.Constants;

import javax.annotation.Nullable;
import java.util.List;

@Mod.EventBusSubscriber(modid = Constants.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class VoidChunkGenerator extends ChunkGenerator<VoidChunkGenerator.Settings> {
	public static final DeferredRegister<ChunkGeneratorType<?, ?>> REGISTER = DeferredRegister.create(ForgeRegistries.CHUNK_GENERATOR_TYPES, Constants.MODID);

	public static final RegistryObject<ChunkGeneratorType<Settings, VoidChunkGenerator>> TYPE = REGISTER.register(
			"void", () -> {
				return new ChunkGeneratorType<Settings, VoidChunkGenerator>(VoidChunkGenerator::new, false, () -> Settings.INSTANCE);
			}
	);

	public VoidChunkGenerator(IWorld world, BiomeProvider biomes, Settings settings) {
		super(world, biomes, settings);
	}

	public VoidChunkGenerator(IWorld world) {
		this(world, biomeProvider(world), Settings.INSTANCE);
	}

	public static BiomeProvider biomeProvider(IWorld world) {
		SingleBiomeProviderSettings settings = new SingleBiomeProviderSettings(world.getWorldInfo())
				.setBiome(Biomes.THE_VOID);
		return new SingleBiomeProvider(settings);
	}

	@Override
	public void makeBase(IWorld world, IChunk chunk) {
	}

	@Override
	public void generateSurface(WorldGenRegion region, IChunk chunk) {
	}

	@Override
	public void func_225550_a_(BiomeManager biomeManager, IChunk chunk, GenerationStage.Carving stage) {
	}

	@Override
	public void decorate(WorldGenRegion region) {
	}

	@Override
	public int getGroundHeight() {
		return 64;
	}

	@Override
	public int func_222529_a(int x, int z, Heightmap.Type heightmap) {
		return 0;
	}

	@Override
	public List<Biome.SpawnListEntry> getPossibleCreatures(EntityClassification creatureType, BlockPos pos) {
		return ImmutableList.of();
	}

	@Override
	public void generateStructureStarts(IWorld world, IChunk chunk) {
	}

	@Override
	public void generateStructures(BiomeManager biomeManager, IChunk chunk, ChunkGenerator<?> generator, TemplateManager templateManager) {
	}

	@Nullable
	@Override
	public BlockPos findNearestStructure(World world, String name, BlockPos pos, int radius, boolean skipExistingChunks) {
		return null;
	}

	@Override
	public int getSeaLevel() {
		return 0;
	}

	public static class Settings extends GenerationSettings {
		public static final Settings INSTANCE = new Settings();

		private Settings() {
		}
	}
}
