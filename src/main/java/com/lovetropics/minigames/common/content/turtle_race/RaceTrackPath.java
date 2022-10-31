package com.lovetropics.minigames.common.content.turtle_race;

import net.minecraft.util.Mth;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RaceTrackPath {
	private final Segment[] segments;
	private final float length;

	private RaceTrackPath(Segment[] segments, float length) {
		this.segments = segments;
		this.length = length;
	}

	public static Builder builder() {
		return new Builder();
	}

	// TODO: This is super inefficient. We can optimize it, but it's a bit of work.
	public Point closestPointAt(int x, int z, float lastPosition) {
		int closestDistanceSq = Integer.MAX_VALUE;
		Point closestPoint = null;

		for (Segment segment : segments) {
			Point point = segment.closestPointAt(x, z);
			int positionBias = (int) Mth.abs(point.position() - lastPosition);
			int distanceSq = point.distanceToSq(x, z) + positionBias;
			if (distanceSq < closestDistanceSq) {
				closestDistanceSq = distanceSq;
				closestPoint = point;
			}
		}

		return Objects.requireNonNull(closestPoint);
	}

	public float length() {
		return length;
	}

	private record Segment(Point start, Point end, int deltaX, int deltaZ, int lengthSq) {
		public Point closestPointAt(int x, int z) {
			int distanceAlongSq = distanceAlongSq(x, z);
			if (distanceAlongSq <= 0) {
				return start;
			} else if (distanceAlongSq >= lengthSq) {
				return end;
			}

			float progressAlong = (float) distanceAlongSq / lengthSq;
			return new Point(
					start.x() + Math.floorDiv(deltaX * distanceAlongSq, lengthSq),
					start.z() + Math.floorDiv(deltaZ * distanceAlongSq, lengthSq),
					Mth.lerp(progressAlong, start.position(), end.position())
			);
		}

		public int distanceAlongSq(int x, int z) {
			int relativeX = x - start.x();
			int relativeZ = z - start.z();
			return relativeX * deltaX + relativeZ * deltaZ;
		}
	}

	public record Point(int x, int z, float position) {
		public int distanceToSq(Point point) {
			return distanceToSq(point.x(), point.z());
		}

		public int distanceToSq(int x, int z) {
			return lengthSq(x - this.x, z - this.z);
		}

		private static int lengthSq(int x, int z) {
			return x * x + z * z;
		}
	}

	public static class Builder {
		private final List<Segment> segments = new ArrayList<>();
		@Nullable
		private Point lastPoint;
		private float length;

		public Builder addPoint(int x, int z) {
			Point point = new Point(x, z, length);
			if (lastPoint != null) {
				length += addSegment(lastPoint, point);
			}
			lastPoint = point;
			return this;
		}

		private float addSegment(Point start, Point end) {
			int deltaX = end.x() - start.x();
			int deltaZ = end.z() - start.z();
			int lengthSq = start.distanceToSq(end);
			segments.add(new Segment(start, end, deltaX, deltaZ, lengthSq));
			return Mth.sqrt(lengthSq);
		}

		public RaceTrackPath build() {
			if (lastPoint == null || segments.isEmpty()) {
				throw new IllegalStateException("Must have at least one track segment");
			}
			return new RaceTrackPath(segments.toArray(Segment[]::new), length);
		}
	}
}
