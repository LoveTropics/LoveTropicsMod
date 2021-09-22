package com.lovetropics.minigames.client.screen.flex;

public enum Axis {
	X, Y;

	public Axis cross() {
		return this == X ? Y : X;
	}
}
