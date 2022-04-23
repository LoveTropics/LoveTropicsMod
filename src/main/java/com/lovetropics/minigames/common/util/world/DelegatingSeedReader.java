package com.lovetropics.minigames.common.util.world;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.particles.IParticleData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.SectionPos;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.DimensionType;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.ITickList;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.lighting.WorldLightManager;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.IWorldInfo;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class DelegatingSeedReader implements ISeedReader {
	protected final ISeedReader parent;

	protected DelegatingSeedReader(ISeedReader parent) {
		this.parent = parent;
	}

	@Override
	public long getSeed() {
		return this.parent.getSeed();
	}

	@Override
	public Stream<? extends StructureStart<?>> startsForFeature(SectionPos sectionPos, Structure<?> structure) {
		return this.parent.startsForFeature(sectionPos, structure);
	}

	@Override
	public ServerWorld getLevel() {
		return this.parent.getLevel();
	}

	@Override
	public ITickList<Block> getBlockTicks() {
		return this.parent.getBlockTicks();
	}

	@Override
	public ITickList<Fluid> getLiquidTicks() {
		return this.parent.getLiquidTicks();
	}

	@Override
	public IWorldInfo getLevelData() {
		return this.parent.getLevelData();
	}

	@Override
	public DifficultyInstance getCurrentDifficultyAt(BlockPos pos) {
		return this.parent.getCurrentDifficultyAt(pos);
	}

	@Override
	public AbstractChunkProvider getChunkSource() {
		return this.parent.getChunkSource();
	}

	@Override
	public Random getRandom() {
		return this.parent.getRandom();
	}

	@Override
	public void playSound(@Nullable PlayerEntity player, BlockPos pos, SoundEvent sound, SoundCategory category, float volume, float pitch) {
		this.parent.playSound(player, pos, sound, category, volume, pitch);
	}

	@Override
	public void addParticle(IParticleData particle, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
		this.parent.addParticle(particle, x, y, z, xSpeed, ySpeed, zSpeed);
	}

	@Override
	public void levelEvent(@Nullable PlayerEntity player, int type, BlockPos pos, int data) {
		this.parent.levelEvent(player, type, pos, data);
	}

	@Override
	public DynamicRegistries registryAccess() {
		return this.parent.registryAccess();
	}

	@Override
	public float getShade(Direction direction, boolean b) {
		return this.parent.getShade(direction, b);
	}

	@Override
	public WorldLightManager getLightEngine() {
		return this.parent.getLightEngine();
	}

	@Override
	public WorldBorder getWorldBorder() {
		return this.parent.getWorldBorder();
	}

	@Nullable
	@Override
	public TileEntity getBlockEntity(BlockPos pos) {
		return this.parent.getBlockEntity(pos);
	}

	@Override
	public BlockState getBlockState(BlockPos pos) {
		return this.parent.getBlockState(pos);
	}

	@Override
	public FluidState getFluidState(BlockPos pos) {
		return this.parent.getFluidState(pos);
	}

	@Override
	public List<Entity> getEntities(@Nullable Entity entity, AxisAlignedBB box, @Nullable Predicate<? super Entity> predicate) {
		return this.parent.getEntities(entity, box, predicate);
	}

	@Override
	public <T extends Entity> List<T> getEntitiesOfClass(Class<? extends T> clazz, AxisAlignedBB box, @Nullable Predicate<? super T> filter) {
		return this.parent.getEntitiesOfClass(clazz, box, filter);
	}

	@Override
	public List<? extends PlayerEntity> players() {
		return this.parent.players();
	}

	@Nullable
	@Override
	public IChunk getChunk(int x, int z, ChunkStatus requiredStatus, boolean create) {
		return this.parent.getChunk(x, z, requiredStatus, create);
	}

	@Override
	public int getHeight(Heightmap.Type type, int x, int z) {
		return this.parent.getHeight(type, x, z);
	}

	@Override
	public int getSkyDarken() {
		return this.parent.getSkyDarken();
	}

	@Override
	public BiomeManager getBiomeManager() {
		return this.parent.getBiomeManager();
	}

	@Override
	public Biome getUncachedNoiseBiome(int x, int y, int z) {
		return this.parent.getUncachedNoiseBiome(x, y, z);
	}

	@Override
	public boolean isClientSide() {
		return this.parent.isClientSide();
	}

	@Override
	public int getSeaLevel() {
		return this.parent.getSeaLevel();
	}

	@Override
	public DimensionType dimensionType() {
		return this.parent.dimensionType();
	}

	@Override
	public boolean setBlock(BlockPos pos, BlockState state, int flags, int recursionLeft) {
		return this.parent.setBlock(pos, state, flags, recursionLeft);
	}

	@Override
	public boolean removeBlock(BlockPos pos, boolean isMoving) {
		FluidState fluid = this.getFluidState(pos);
		int flags = Constants.BlockFlags.DEFAULT | (isMoving ? Constants.BlockFlags.IS_MOVING : 0);
		return this.setBlock(pos, fluid.createLegacyBlock(), flags);
	}

	@Override
	public boolean destroyBlock(BlockPos pos, boolean dropBlock, @Nullable Entity entity, int recursionLeft) {
		BlockState block = this.getBlockState(pos);
		if (!block.isAir(this, pos)) {
			FluidState fluid = this.getFluidState(pos);
			return this.setBlock(pos, fluid.createLegacyBlock(), Constants.BlockFlags.DEFAULT, recursionLeft);
		}
		return false;
	}

	@Override
	public boolean isStateAtPosition(BlockPos pos, Predicate<BlockState> predicate) {
		return predicate.test(this.getBlockState(pos));
	}
}
