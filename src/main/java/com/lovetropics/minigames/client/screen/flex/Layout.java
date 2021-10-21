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
