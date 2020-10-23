package com.lovetropics.minigames.common.minigames.behaviours;

import com.mojang.datafixers.Dynamic;
import net.minecraftforge.registries.IForgeRegistryEntry;

public interface IMinigameBehaviorType<T> extends IForgeRegistryEntry<IMinigameBehaviorType<?>> {
	
	<D> T create(Dynamic<D> data);

	@SuppressWarnings({ "unchecked", "rawtypes" })
	static Class<IMinigameBehaviorType<?>> wildcardType() {
		return (Class<IMinigameBehaviorType<?>>) (Class) IMinigameBehaviorType.class;
	}
}
