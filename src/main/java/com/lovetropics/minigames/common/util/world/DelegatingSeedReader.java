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
	public Stream<? extends StructureStart<?>> func_241827_a(SectionPos sectionPos, Structure<?> structure) {
		return this.parent.func_241827_a(sectionPos, structure);
	}

	@Override
	public ServerWorld getWorld() {
		return this.parent.getWorld();
	}

	@Override
	public ITickList<Block> getPendingBlockTicks() {
		return this.parent.getPendingBlockTicks();
	}

	@Override
	public ITickList<Fluid> getPendingFluidTicks() {
		return this.parent.getPendingFluidTicks();
	}

	@Override
	public IWorldInfo getWorldInfo() {
		return this.parent.getWorldInfo();
	}

	@Override
	public DifficultyInstance getDifficultyForLocation(BlockPos pos) {
		return this.parent.getDifficultyForLocation(pos);
	}

	@Override
	public AbstractChunkProvider getChunkProvider() {
		return this.parent.getChunkProvider();
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
	public void playEvent(@Nullable PlayerEntity player, int type, BlockPos pos, int data) {
		this.parent.playEvent(player, type, pos, data);
	}

	@Override
	public DynamicRegistries func_241828_r() {
		return this.parent.func_241828_r();
	}

	@Override
	public float func_230487_a_(Direction direction, boolean b) {
		return this.parent.func_230487_a_(direction, b);
	}

	@Override
	public WorldLightManager getLightManager() {
		return this.parent.getLightManager();
	}

	@Override
	public WorldBorder getWorldBorder() {
		return this.parent.getWorldBorder();
	}

	@Nullable
	@Override
	public TileEntity getTileEntity(BlockPos pos) {
		return this.parent.getTileEntity(pos);
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
	public List<Entity> getEntitiesInAABBexcluding(@Nullable Entity entity, AxisAlignedBB box, @Nullable Predicate<? super Entity> predicate) {
		return this.parent.getEntitiesInAABBexcluding(entity, box, predicate);
	}

	@Override
	public <T extends Entity> List<T> getEntitiesWithinAABB(Class<? extends T> clazz, AxisAlignedBB box, @Nullable Predicate<? super T> filter) {
		return this.parent.getEntitiesWithinAABB(clazz, box, filter);
	}

	@Override
	public List<? extends PlayerEntity> getPlayers() {
		return this.parent.getPlayers();
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
	public int getSkylightSubtracted() {
		return this.parent.getSkylightSubtracted();
	}

	@Override
	public BiomeManager getBiomeManager() {
		return this.parent.getBiomeManager();
	}

	@Override
	public Biome getNoiseBiomeRaw(int x, int y, int z) {
		return this.parent.getNoiseBiomeRaw(x, y, z);
	}

	@Override
	public boolean isRemote() {
		return this.parent.isRemote();
	}

	@Override
	public int getSeaLevel() {
		return this.parent.getSeaLevel();
	}

	@Override
	public DimensionType getDimensionType() {
		return this.parent.getDimensionType();
	}

	@Override
	public boolean setBlockState(BlockPos pos, BlockState state, int flags, int recursionLeft) {
		return this.parent.setBlockState(pos, state, flags, recursionLeft);
	}

	@Override
	public boolean removeBlock(BlockPos pos, boolean isMoving) {
		return this.parent.removeBlock(pos, isMoving);
	}

	@Override
	public boolean destroyBlock(BlockPos pos, boolean dropBlock, @Nullable Entity entity, int recursionLeft) {
		return this.parent.destroyBlock(pos, dropBlock, entity, recursionLeft);
	}

	@Override
	public boolean hasBlockState(BlockPos pos, Predicate<BlockState> predicate) {
		return this.parent.hasBlockState(pos, predicate);
	}
}
