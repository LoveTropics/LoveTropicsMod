package com.lovetropics.minigames.common.content.biodiversity_blitz.entity;

import net.minecraft.entity.Entity;
import net.minecraft.util.Direction;
import net.minecraft.util.ReuseableStream;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class PlotWalls {
	private final AxisAlignedBB bounds;
	private final AxisAlignedBB[] faces;
	private final VoxelShape[] faceShapes;

	public PlotWalls(AxisAlignedBB bounds) {
		this.bounds = bounds;

		Direction[] directions = Direction.values();
		this.faces = new AxisAlignedBB[directions.length];
		this.faceShapes = new VoxelShape[directions.length];

		for (Direction direction : directions) {
			AxisAlignedBB plotFace = createWallBounds(bounds, direction);

			int index = direction.getIndex();
			this.faces[index] = plotFace;
			this.faceShapes[index] = VoxelShapes.create(plotFace);
		}
	}

	private AxisAlignedBB createWallBounds(AxisAlignedBB bounds, Direction direction) {
		Direction.Axis axis = direction.getAxis();
		double size = bounds.getMax(axis) - bounds.getMin(axis);

		// offset to the edge in this direction
		Vector3d offset = Vector3d.copy(direction.getDirectionVec()).scale(size);

		// grow on every other axis to not create any holes
		Vector3d grow = new Vector3d(
				axis != Direction.Axis.X ? bounds.getXSize() : 0.0,
				axis != Direction.Axis.Y ? bounds.getYSize() : 0.0,
				axis != Direction.Axis.Z ? bounds.getZSize() : 0.0
		);

		return bounds
				.offset(offset)
				.grow(grow.x, grow.y, grow.z);
	}

	public Vector3d collide(AxisAlignedBB box, Vector3d offset) {
		if (offset.lengthSquared() == 0.0) {
			return offset;
		}

		AxisAlignedBB collidingBox = box.expand(offset);

		// we're definitely not going to collide
		if (!collidingBox.intersects(this.bounds)) {
			return offset;
		}

		Stream<VoxelShape> collisions = this.collisions(collidingBox);
		return Entity.collideBoundingBox(offset, box, new ReuseableStream<>(collisions));
	}

	public Stream<VoxelShape> collisions(AxisAlignedBB box) {
		CollisionSpliterator spliterator = new CollisionSpliterator(box, faces, faceShapes);
		return StreamSupport.stream(spliterator, false);
	}

	public AxisAlignedBB getBounds() {
		return this.bounds;
	}

	public static final class CollisionSpliterator extends Spliterators.AbstractSpliterator<VoxelShape> {
		private final AxisAlignedBB box;
		private final AxisAlignedBB[] faces;
		private final VoxelShape[] faceShapes;

		private int faceIndex;

		CollisionSpliterator(AxisAlignedBB box, AxisAlignedBB[] faces, VoxelShape[] faceShapes) {
			super(Long.MAX_VALUE, Spliterator.NONNULL | Spliterator.IMMUTABLE);
			this.box = box;
			this.faces = faces;
			this.faceShapes = faceShapes;
		}

		@Override
		public boolean tryAdvance(Consumer<? super VoxelShape> action) {
			AxisAlignedBB[] faces = this.faces;
			while (this.faceIndex < faces.length) {
				int index = this.faceIndex++;
				if (this.box.intersects(faces[index])) {
					action.accept(this.faceShapes[index]);
					return true;
				}
			}

			return false;
		}
	}
}
