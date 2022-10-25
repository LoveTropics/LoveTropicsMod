package com.lovetropics.minigames.common.util;

import it.unimi.dsi.fastutil.floats.Float2FloatFunction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class LinearSpline implements Float2FloatFunction {
	private final float[] xs;
	private final float[] ys;
	private final float[] gradients;

	private LinearSpline(float[] xs, float[] ys, float[] gradients) {
		this.xs = xs;
		this.ys = ys;
		this.gradients = gradients;
	}

	public static Builder builder() {
		return new Builder();
	}

	@Override
	public float get(float x) {
		int index = indexAt(x);
		if (index <= 0) {
			return ys[0];
		} else if (index >= ys.length) {
			return ys[ys.length - 1];
		}

		float x0 = xs[index];
		float y0 = ys[index];
		float gradient = gradients[index - 1];
		return (x - x0) * gradient + y0;
	}

	private int indexAt(float x) {
		int i = Arrays.binarySearch(xs, x);
		return i >= 0 ? i : -i - 1;
	}

	public static class Builder {
		private final List<Point> points = new ArrayList<>();

		private Builder() {
		}

		public Builder point(float x, float y) {
			points.add(new Point(x, y));
			return this;
		}

		public LinearSpline build() {
			if (points.size() < 1) {
				throw new IllegalStateException("Cannot have less than 1 point");
			}

			float[] xs = new float[points.size()];
			float[] ys = new float[points.size()];
			float[] gradients = new float[points.size() - 1];

			points.sort(Comparator.comparingDouble(Point::x));

			for (int i = 0; i < points.size(); i++) {
				Point point = points.get(i);
				xs[i] = point.x();
				ys[i] = point.y();
			}

			for (int i = 0; i < points.size() - 1; i++) {
				Point start = points.get(i);
				Point end = points.get(i + 1);
				gradients[i] = (end.y() - start.y()) / (end.x() - start.x());
			}

			return new LinearSpline(xs, ys, gradients);
		}
	}

	private record Point(float x, float y) {
	}
}
