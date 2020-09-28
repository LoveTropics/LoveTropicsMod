package com.lovetropics.minigames.common.minigames.map;

import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehaviorType;
import com.mojang.datafixers.Dynamic;
import net.minecraftforge.registries.ForgeRegistryEntry;

public final class MinigameMapProviderType<T extends IMinigameMapProvider> extends ForgeRegistryEntry<MinigameMapProviderType<?>> {
	private final MinigameMapProviderType.Factory<T> instanceFactory;

	public MinigameMapProviderType(final MinigameMapProviderType.Factory<T> instanceFactory) {
		this.instanceFactory = instanceFactory;
	}

	public <D> T create(Dynamic<D> data) {
		return instanceFactory.create(data);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	static Class<MinigameMapProviderType<?>> wildcardType() {
		return (Class<MinigameMapProviderType<?>>) (Class) MinigameMapProviderType.class;
	}

	public interface Factory<T extends IMinigameMapProvider> {
		<D> T create(Dynamic<D> data);
	}
}
