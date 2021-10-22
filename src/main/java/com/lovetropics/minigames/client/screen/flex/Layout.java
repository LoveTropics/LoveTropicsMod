package com.lovetropics.minigames.client.screen.flex;

import java.util.Objects;

public final class Layout {
	private final Box content;
	private final Box padding;
	private final Box margin;

	public Layout(Box content, Box padding, Box margin) {
		this.content = content;
		this.padding = padding;
		this.margin = margin;
	}

	public Box content() {
		return this.content;
	}

	public Box padding() {
		return this.padding;
	}

	public Box margin() {
		return this.margin;
	}

	public Box background() {
		return this.padding;
	}

	public Layout clip(Box bounds) {
		Box origArea = margin();
		Box newArea = origArea.intersect(bounds);
		int dl = newArea.left() - origArea.left();
		int dr = newArea.right() - origArea.right();
		int dt = newArea.top() - origArea.top();
		int db = newArea.bottom() - origArea.bottom();
		return new Layout(content().grow(-dl, -dt, dr, db), padding().grow(-dl, -dt, dr, db), newArea);
	}

	public Layout shrinkTo(Box bounds) {
		Box origArea = content();
		Box newArea = bounds;
		int dl = newArea.left() - origArea.left();
		int dr = newArea.right() - origArea.right();
		int dt = newArea.top() - origArea.top();
		int db = newArea.bottom() - origArea.bottom();
		return new Layout(newArea, padding().grow(-dl, -dt, dr, db), margin().grow(-dl, -dt, dr, db));
	}

	public Layout moveY(int y) {
		int dy = y - margin().top();
		return new Layout(content().shift(0, dy), padding().shift(0, dy), margin().shift(0, dy));
	}

	@Override
	public String toString() {
		return "Layout [content=" + content + ", padding=" + padding + ", margin=" + margin + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(content, margin, padding);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Layout other = (Layout) obj;
		return Objects.equals(content, other.content) && Objects.equals(margin, other.margin)
				&& Objects.equals(padding, other.padding);
	}
}
