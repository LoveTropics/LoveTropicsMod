package com.lovetropics.minigames.client.screen.flex;

import java.util.Objects;

import net.minecraft.client.gui.screen.Screen;

public final class Box {

	private final int left;
	private final int top;
	private final int right;
	private final int bottom;

	public Box(int left, int top, int right, int bottom) {
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
	}

	public Box(Size size) {
		this(0, 0, size.width(), size.height());
	}

	public Box(Screen screen) {
		this(0, 0, screen.width, screen.height);
	}

	public Box grow(int amount) {
		return this.grow(amount, amount);
	}

	public Box grow(int x, int y) {
		return this.grow(x, y, x, y);
	}

	public Box grow(int left, int top, int right, int bottom) {
		return new Box(
				this.left - left,
				this.top - top,
				this.right + right,
				this.bottom + bottom
		);
	}

	public Box contract(Box border) {
		return this.grow(-border.left(), -border.top(), -border.right(), -border.bottom());
	}

	public int left() {
		return this.left;
	}

	public int top() {
		return this.top;
	}

	public int right() {
		return this.right;
	}

	public int bottom() {
		return this.bottom;
	}

	public int width() {
		return this.right - this.left;
	}

	public int height() {
		return this.bottom - this.top;
	}

	public Size size() {
		return new Size(this.width(), this.height());
	}

	public int centerX() {
		return (this.left + this.right) / 2;
	}

	public int centerY() {
		return (this.top + this.bottom) / 2;
	}

	public int borderX() {
		return this.left + this.right;
	}

	public int borderY() {
		return this.top + this.bottom;
	}

	public Box left(int left) {
		return new Box(left, this.top, this.right, this.bottom);
	}

	public Box top(int top) {
		return new Box(this.left, top, this.right, this.bottom);
	}

	public Box right(int right) {
		return new Box(this.left, this.top, right, this.bottom);
	}

	public Box bottom(int bottom) {
		return new Box(this.left, this.top, this.right, bottom);
	}

	static Box combine(Axis mainAxis, Interval main, Interval cross) {
		if (mainAxis == Axis.X) {
			return new Box(main.start, cross.start, main.end, cross.end);
		} else {
			return new Box(cross.start, main.start, cross.end, main.end);
		}
	}

	int borderAlong(Axis axis) {
		return axis == Axis.X ? this.borderX() : this.borderY();
	}

	Interval along(Axis axis) {
		return axis == Axis.X ? new Interval(this.left, this.right) : new Interval(this.top, this.bottom);
	}

	public boolean contains(double x, double y) {
		return x >= this.left && y >= this.top && x < this.right && y < this.bottom;
	}

	@Override
	public String toString() {
		return "Box [" + left + "," + top + " -> " + right + "," + bottom + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(bottom, left, right, top);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Box other = (Box) obj;
		return bottom == other.bottom && left == other.left && right == other.right && top == other.top;
	}

	static final class Size {
		private final int width;
		private final int height;

		public Size(int width, int height) {
			this.width = width;
			this.height = height;
		}

		public Size grow(int amount) {
			return this.grow(amount, amount);
		}

		public Size grow(int x, int y) {
			return new Size(this.width + x, this.height + y);
		}

		public Size grow(Axis axis, int amount) {
			return axis == Axis.X ? this.grow(amount, 0) : this.grow(0, amount);
		}

		public Size grow(Box border) {
			return this.grow(border.borderX(), border.borderY());
		}

		public int width() {
			return this.width;
		}

		public int height() {
			return this.height;
		}

		public int along(Axis axis) {
			switch (axis) {
				case X: return this.width;
				case Y: return this.height;
				default: throw new UnsupportedOperationException();
			}
		}
	}

	static final class Interval {
		final int start;
		final int end;

		Interval(int start, int end) {
			this.start = start;
			this.end = end;
		}

		Interval subtract(Align.Main align, int size) {
			switch (align) {
				case START: return new Interval(this.start + size, this.end);
				case END: return new Interval(this.start, this.end - size);
				default: throw new UnsupportedOperationException();
			}
		}

		int size() {
			return this.end - this.start;
		}

		Interval applyMainAlign(Align.Main align, int size) {
			switch (align) {
				case START: return new Interval(this.start, this.start + size);
				case END: return new Interval(this.end - size, this.end);
				default: throw new UnsupportedOperationException();
			}
		}

		Interval applyCrossAlign(Align.Cross align, int size) {
			switch (align) {
				case START: return new Interval(this.start, this.start + size);
				case CENTER: {
					int start = (this.start + this.end - size) / 2;
					return new Interval(start, start + size);
				}
				case END: return new Interval(this.end - size, this.end);
				default: throw new UnsupportedOperationException();
			}
		}
	}
}
