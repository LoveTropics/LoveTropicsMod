package com.lovetropics.minigames.common.content.biodiversity_blitz.entity;

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
			Direction.Axis axis = direction.getAxis();
			double size = bounds.getMax(axis) - bounds.getMin(axis);
			Vector3d offset = Vector3d.copy(direction.getDirectionVec()).scale(size);
			AxisAlignedBB plotFace = bounds.offset(offset).grow(0.1);

			int index = direction.getIndex();
			this.faces[index] = plotFace;
			this.faceShapes[index] = VoxelShapes.create(plotFace);
		}
	}

	public Vector3d collide(AxisAlignedBB box, Vector3d offset) {
		if (offset.lengthSquared() == 0.0) {
			return offset;
		}

		ReuseableStream<VoxelShape> collisions = new ReuseableStream<>(this.collisions(box.expand(offset)));

		double dx = offset.x;
		double dy = offset.y;
		double dz = offset.z;
		if (dx != 0.0) dx = VoxelShapes.getAllowedOffset(Direction.Axis.X, box, collisions.createStream(), dx);
		if (dy != 0.0) dy = VoxelShapes.getAllowedOffset(Direction.Axis.Y, box, collisions.createStream(), dy);
		if (dz != 0.0) dz = VoxelShapes.getAllowedOffset(Direction.Axis.Z, box, collisions.createStream(), dz);

		if (dx != offset.x || dy != offset.y || dz != offset.z) {
			return new Vector3d(dx, dy, dz);
		} else {
			return offset;
		}
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
		private boolean consumed;

		CollisionSpliterator(AxisAlignedBB box, AxisAlignedBB[] faces, VoxelShape[] faceShapes) {
			super(Long.MAX_VALUE, Spliterator.NONNULL | Spliterator.IMMUTABLE);
			this.box = box;
			this.faces = faces;
			this.faceShapes = faceShapes;
		}

		@Override
		public boolean tryAdvance(Consumer<? super VoxelShape> action) {
			if (this.consumed) {
				return false;
			}

			this.consumed = true;

			AxisAlignedBB[] faces = this.faces;
			for (int i = 0; i < faces.length; i++) {
				if (this.box.intersects(faces[i])) {
					action.accept(this.faceShapes[i]);
					return true;
				}
			}

			return false;
		}
	}
}
