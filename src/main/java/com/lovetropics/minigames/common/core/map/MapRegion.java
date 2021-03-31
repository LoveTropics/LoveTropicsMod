package com.lovetropics.minigames.common.core.map;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Random;

public final class MapRegion implements Iterable<BlockPos> {
	public final BlockPos min;
	public final BlockPos max;

	private MapRegion(BlockPos min, BlockPos max) {
		this.min = min;
		this.max = max;
	}

	public static MapRegion of(BlockPos pos) {
		return new MapRegion(pos, pos);
	}

	public static MapRegion of(BlockPos a, BlockPos b) {
		return new MapRegion(MapRegion.min(a, b), MapRegion.max(a, b));
	}

	public static MapRegion ofChunk(int chunkX, int chunkZ) {
		return new MapRegion(
				new BlockPos(chunkX << 4, 0, chunkZ << 4),
				new BlockPos((chunkX << 4) + 15, 256, (chunkZ << 16) + 15)
		);
	}

	public static MapRegion ofChunk(ChunkPos chunkPos) {
		return ofChunk(chunkPos.x, chunkPos.z);
	}

	public static BlockPos min(BlockPos a, BlockPos b) {
		return new BlockPos(
				Math.min(a.getX(), b.getX()),
				Math.min(a.getY(), b.getY()),
				Math.min(a.getZ(), b.getZ())
		);
	}

	public static BlockPos max(BlockPos a, BlockPos b) {
		return new BlockPos(
				Math.max(a.getX(), b.getX()),
				Math.max(a.getY(), b.getY()),
				Math.max(a.getZ(), b.getZ())
		);
	}

	public MapRegion withMin(BlockPos min) {
		return MapRegion.of(min, max);
	}

	public MapRegion withMax(BlockPos max) {
		return MapRegion.of(min, max);
	}

	public MapRegion offset(double x, double y, double z) {
		return new MapRegion(
				min.add(x, y, z),
				max.add(x, y, z)
		);
	}

	public Vector3d getCenter() {
		return new Vector3d(
				(min.getX() + max.getX() + 1.0) / 2.0,
				(min.getY() + max.getY() + 1.0) / 2.0,
				(min.getZ() + max.getZ() + 1.0) / 2.0
		);
	}

	public BlockPos getCenterBlock() {
		return new BlockPos(
				(min.getX() + max.getX() + 1) / 2,
				(min.getY() + max.getY() + 1) / 2,
				(min.getZ() + max.getZ() + 1) / 2
		);
	}

	public BlockPos getSize() {
		return new BlockPos(
				max.getX() - min.getX() + 1,
				max.getY() - min.getY() + 1,
				max.getZ() - min.getZ() + 1
		);
	}

	public long getVolume() {
		long sizeX = max.getX() - min.getX() + 1;
		long sizeY = max.getY() - min.getY() + 1;
		long sizeZ = max.getZ() - min.getZ() + 1;
		return sizeX * sizeY * sizeZ;
	}

	public BlockPos sample(Random random) {
		return new BlockPos(
				min.getX() + random.nextInt(max.getX() - min.getX() + 1),
				min.getY() + random.nextInt(max.getY() - min.getY() + 1),
				min.getZ() + random.nextInt(max.getZ() - min.getZ() + 1)
		);
	}

	public boolean contains(BlockPos pos) {
		return contains(pos.getX(), pos.getY(), pos.getZ());
	}

	public boolean contains(Vector3d pos) {
		return contains(pos.x, pos.y, pos.z);
	}

	public boolean contains(double x, double y, double z) {
		return x >= min.getX() && y >= min.getY() && z >= min.getZ() && x <= max.getX() && y <= max.getY() && z <= max.getZ();
	}

	public boolean contains(int x, int y, int z) {
		return x >= min.getX() && y >= min.getY() && z >= min.getZ() && x <= max.getX() && y <= max.getY() && z <= max.getZ();
	}

	public boolean intersects(AxisAlignedBB bounds) {
		return bounds.intersects(min.getX(), min.getY(), min.getZ(), max.getX() + 1.0, max.getY() + 1.0, max.getZ() + 1.0);
	}

	@Nullable
	public MapRegion intersection(MapRegion other) {
		BlockPos min = max(this.min, other.min);
		BlockPos max = min(this.max, other.max);
		if (min.getX() >= max.getX() || min.getY() >= max.getY() || min.getZ() >= max.getZ()) {
			return null;
		}
		return new MapRegion(min, max);
	}

	public AxisAlignedBB toAabb() {
		return new AxisAlignedBB(min.getX(), min.getY(), min.getZ(), max.getX() + 1.0, max.getY() + 1.0, max.getZ() + 1.0);
	}

	@Override
	public Iterator<BlockPos> iterator() {
		return BlockPos.getAllInBoxMutable(min, max).iterator();
	}

	public LongSet asChunks() {
		LongSet chunks = new LongOpenHashSet();

		int minChunkX = min.getX() >> 4;
		int minChunkZ = min.getZ() >> 4;
		int maxChunkX = max.getX() >> 4;
		int maxChunkZ = max.getZ() >> 4;

		for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
			for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
				chunks.add(ChunkPos.asLong(chunkX, chunkZ));
			}
		}

		return chunks;
	}

	public CompoundNBT write(CompoundNBT root) {
		root.put("min", writeBlockPos(min, new CompoundNBT()));
		root.put("max", writeBlockPos(max, new CompoundNBT()));
		return root;
	}

	public static MapRegion read(CompoundNBT root) {
		BlockPos min = readBlockPos(root.getCompound("min"));
		BlockPos max = readBlockPos(root.getCompound("max"));
		return new MapRegion(min, max);
	}

	public void write(PacketBuffer buffer) {
		buffer.writeBlockPos(min);
		buffer.writeBlockPos(max);
	}

	public static MapRegion read(PacketBuffer buffer) {
		BlockPos min = buffer.readBlockPos();
		BlockPos max = buffer.readBlockPos();
		return new MapRegion(min, max);
	}

	private static CompoundNBT writeBlockPos(BlockPos pos, CompoundNBT root) {
		root.putInt("x", pos.getX());
		root.putInt("y", pos.getY());
		root.putInt("z", pos.getZ());
		return root;
	}

	private static BlockPos readBlockPos(CompoundNBT root) {
		return new BlockPos(root.getInt("x"), root.getInt("y"), root.getInt("z"));
	}
}
