package com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.state;

public final class PlantHealth {
	public static final PlantState.Key<PlantHealth> KEY = PlantState.Key.create();

	private int health;

	public PlantHealth(int health) {
		this.health = health;
	}

	public void decrement(int amount) {
		this.health = Math.max(0, this.health - amount);
	}

	public boolean isDead() {
		return this.health <= 0;
	}

	public int health() {
		return this.health;
	}
}
