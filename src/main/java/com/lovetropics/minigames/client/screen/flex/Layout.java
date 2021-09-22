package com.lovetropics.minigames.client.screen.flex;

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
}
