package com.lovetropics.minigames.common.minigames.map;

import com.mojang.datafixers.Dynamic;
import net.minecraftforge.registries.ForgeRegistryEntry;

public final class MinigameMapProviderType extends ForgeRegistryEntry<MinigameMapProviderType> {
	private final MinigameMapProviderType.Factory instanceFactory;

	public MinigameMapProviderType(final MinigameMapProviderType.Factory instanceFactory) {
		this.instanceFactory = instanceFactory;
	}

	public <T> IMinigameMapProvider create(Dynamic<T> data) {
		return instanceFactory.create(data);
	}

	public interface Factory {
		<T> IMinigameMapProvider create(Dynamic<T> data);
	}
}
