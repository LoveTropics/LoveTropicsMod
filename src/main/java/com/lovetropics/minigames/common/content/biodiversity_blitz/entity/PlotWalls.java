package com.lovetropics.minigames.common.content.biodiversity_blitz.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.core.Direction;
import net.minecraft.util.RewindableStream;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.Vec3;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class PlotWalls {
	private final AABB bounds;
	private final AABB[] faces;
	private final VoxelShape[] faceShapes;

	public PlotWalls(AABB bounds) {
		this.bounds = bounds;

		Direction[] directions = Direction.values();
		this.faces = new AABB[directions.length];
		this.faceShapes = new VoxelShape[directions.length];

		for (Direction direction : directions) {
			AABB plotFace = createWallBounds(bounds, direction);

			int index = direction.get3DDataValue();
			this.faces[index] = plotFace;
			this.faceShapes[index] = Shapes.create(plotFace);
		}
	}

	private AABB createWallBounds(AABB bounds, Direction direction) {
		Direction.Axis axis = direction.getAxis();
		double size = bounds.max(axis) - bounds.min(axis);

		// offset to the edge in this direction
		Vec3 offset = Vec3.atLowerCornerOf(direction.getNormal()).scale(size);

		// grow on every other axis to not create any holes
		Vec3 grow = new Vec3(
				axis != Direction.Axis.X ? bounds.getXsize() : 0.0,
				axis != Direction.Axis.Y ? bounds.getYsize() : 0.0,
				axis != Direction.Axis.Z ? bounds.getZsize() : 0.0
		);

		return bounds
				.move(offset)
				.inflate(grow.x, grow.y, grow.z);
	}

	public Vec3 collide(AABB box, Vec3 offset) {
		if (offset.lengthSqr() == 0.0) {
			return offset;
		}

		AABB collidingBox = box.expandTowards(offset);

		// we're definitely not going to collide
		if (!collidingBox.intersects(this.bounds)) {
			return offset;
		}

		Stream<VoxelShape> collisions = this.collisions(collidingBox);
		return Entity.collideBoundingBoxLegacy(offset, box, new RewindableStream<>(collisions));
	}

	public Stream<VoxelShape> collisions(AABB box) {
		CollisionSpliterator spliterator = new CollisionSpliterator(box, faces, faceShapes);
		return StreamSupport.stream(spliterator, false);
	}

	public AABB getBounds() {
		return this.bounds;
	}

	public boolean containsEntity(Entity entity) {
		return this.bounds.contains(entity.position());
	}

	public boolean containsBlock(BlockPos pos) {
		return this.bounds.contains(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
	}

	public static final class CollisionSpliterator extends Spliterators.AbstractSpliterator<VoxelShape> {
		private final AABB box;
		private final AABB[] faces;
		private final VoxelShape[] faceShapes;

		private int faceIndex;

		CollisionSpliterator(AABB box, AABB[] faces, VoxelShape[] faceShapes) {
			super(Long.MAX_VALUE, Spliterator.NONNULL | Spliterator.IMMUTABLE);
			this.box = box;
			this.faces = faces;
			this.faceShapes = faceShapes;
		}

		@Override
		public boolean tryAdvance(Consumer<? super VoxelShape> action) {
			AABB[] faces = this.faces;
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
