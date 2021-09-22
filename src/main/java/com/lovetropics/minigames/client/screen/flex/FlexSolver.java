package com.lovetropics.minigames.client.screen.flex;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;

public final class FlexSolver {
	private final int width;
	private final int height;

	public FlexSolver(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public Results apply(Flex root) {
		Results results = new Results();

		Box.Size size = new Box.Size(this.width, this.height);

		FlexSolve rootSolve = results.flexSolve(root);
		rootSolve.size = size;
		rootSolve.layout = layout(root, new Box(size));

		this.updateInnerSizes(results, root, rootSolve);
		this.solveSizes(results, root, rootSolve);
		this.solveLayouts(results, root, rootSolve);

		return results;
	}

	private void updateInnerSizes(Results results, Flex flex, FlexSolve solve) {
		Flex.Length innerWidth = Flex.Length.ZERO;
		Flex.Length innerHeight = Flex.Length.ZERO;

		for (Flex child : flex.children) {
			FlexSolve childSolve = results.flexSolve(child);
			this.updateInnerSizes(results, child, childSolve);

			if (flex.axis == Axis.X) {
				innerWidth = innerWidth.add(childSolve.outerWidth.min);
				innerHeight = Flex.Length.max(innerHeight, childSolve.outerHeight.min);
			} else {
				innerWidth = Flex.Length.max(innerWidth, childSolve.outerWidth.min);
				innerHeight = innerHeight.add(childSolve.outerHeight.min);
			}
		}

		solve.innerWidth = innerWidth;
		solve.innerHeight = innerHeight;
	}

	private void solveSizes(Results results, Flex flex, FlexSolve solve) {
		Box.Size innerSize = innerSize(flex, solve.size);

		float totalGrow = 0.0F;
		for (Flex child : flex.children) {
			FlexSolve childSolve = results.flexSolve(child);
			childSolve.size = this.solveSize(innerSize, childSolve);
			totalGrow += child.grow;
		}

		if (totalGrow > 0.0F) {
			this.solveGrow(results, flex, solve, totalGrow);
		}

		for (Flex child : flex.children) {
			this.solveSizes(results, child, results.flexSolve(child));
		}
	}

	private void solveGrow(Results results, Flex flex, FlexSolve solve, float totalGrow) {
		int remainingSize = this.remainingSizeForGrow(results, flex, solve);
		if (remainingSize <= 0) return;

		for (Flex child : flex.children) {
			if (child.grow <= 0.0F) continue;

			int growSize = Math.round(remainingSize * (child.grow / totalGrow));
			remainingSize -= growSize;
			totalGrow -= child.grow;

			FlexSolve childSolve = results.flexSolve(child);
			childSolve.size = childSolve.size.grow(flex.axis, growSize);
		}
	}

	private int remainingSizeForGrow(Results results, Flex flex, FlexSolve solve) {
		int remainingSize = solve.size.along(flex.axis);
		for (Flex child : flex.children) {
			FlexSolve childSolve = results.flexSolve(child);
			remainingSize -= childSolve.size.along(flex.axis);
		}
		return remainingSize;
	}

	private void solveLayouts(Results results, Flex flex, FlexSolve solve) {
		Axis axis = flex.axis;

		Box inner = solve.layout.content();
		Box.Interval innerMain = inner.along(axis);
		Box.Interval innerCross = inner.along(axis.cross());

		for (Flex child : flex.children) {
			FlexSolve childSolve = results.flexSolve(child);

			Box.Interval childMain = innerMain.applyMainAlign(child.alignMain, childSolve.size.along(axis));
			Box.Interval childCross = innerCross.applyCrossAlign(child.alignCross, childSolve.size.along(axis.cross()));

			childSolve.layout = layout(child, Box.combine(axis, childMain, childCross));

			innerMain = innerMain.subtract(child.alignMain, childMain.size());

			this.solveLayouts(results, child, childSolve);
		}
	}

	private Box.Size solveSize(Box.Size parent, FlexSolve solve) {
		int minWidth = solve.outerWidth.min.resolve(parent.width());
		int minHeight = solve.outerHeight.min.resolve(parent.height());
		int maxWidth = solve.outerWidth.max.resolve(parent.width());
		int maxHeight = solve.outerHeight.max.resolve(parent.height());

		int innerWidth = solve.innerWidth.resolve(minWidth);
		int innerHeight = solve.innerHeight.resolve(minHeight);

		return new Box.Size(
				MathHelper.clamp(innerWidth, minWidth, maxWidth),
				MathHelper.clamp(innerHeight, minHeight, maxHeight)
		);
	}

	private static Box.Size innerSize(Flex flex, Box.Size size) {
		return size.contract(flex.margin).contract(flex.padding);
	}

	private static Layout layout(Flex flex, Box margin) {
		Box padding = margin.contract(flex.margin);
		Box content = padding.contract(flex.padding);
		return new Layout(content, padding, margin);
	}

	public static final class Results {
		private final Map<Flex, FlexSolve> flexSolves = new Reference2ObjectOpenHashMap<>();

		public Layout layout(Flex flex) {
			return Objects.requireNonNull(this.layoutOrNull(flex), "given flex has not been solved");
		}

		@Nullable
		private Layout layoutOrNull(Flex flex) {
			FlexSolve solve = this.flexSolves.get(flex);
			return solve != null ? solve.layout : null;
		}

		private FlexSolve flexSolve(Flex flex) {
			return this.flexSolves.computeIfAbsent(flex, FlexSolve::new);
		}
	}

	static final class FlexSolve {
		final Flex.LengthRange outerWidth;
		final Flex.LengthRange outerHeight;
		Flex.Length innerWidth = Flex.Length.ZERO;
		Flex.Length innerHeight = Flex.Length.ZERO;

		Box.Size size;
		Layout layout;

		FlexSolve(Flex flex) {
			this.outerWidth = flex.width.add(flex.padding.borderX() + flex.margin.borderX());
			this.outerHeight = flex.height.add(flex.padding.borderY() + flex.margin.borderY());
		}
	}
}
