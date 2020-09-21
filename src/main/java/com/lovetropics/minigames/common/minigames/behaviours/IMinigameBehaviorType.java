package com.lovetropics.minigames.common.minigames.behaviours;

import com.google.gson.JsonElement;
import com.mojang.datafixers.Dynamic;

import net.minecraftforge.registries.IForgeRegistryEntry;

public interface IMinigameBehaviorType<T extends IMinigameBehavior> extends IForgeRegistryEntry<IMinigameBehaviorType<?>> {
	
	T create(Dynamic<JsonElement> data);
}
