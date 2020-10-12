package com.lovetropics.minigames.common.minigames.behaviours;

import com.mojang.datafixers.Dynamic;

public final class ConfiguredBehavior<T extends IMinigameBehavior> {
	public final IMinigameBehaviorType<T> type;
	public final Dynamic<?> config;

	private ConfiguredBehavior(IMinigameBehaviorType<T> type, Dynamic<?> config) {
		this.type = type;
		this.config = config;
	}

	public static <T extends IMinigameBehavior> ConfiguredBehavior<T> of(IMinigameBehaviorType<T> type, Dynamic<?> config) {
		return new ConfiguredBehavior<>(type, config);
	}

	public IMinigameBehavior create() {
		return type.create(config);
	}
}
