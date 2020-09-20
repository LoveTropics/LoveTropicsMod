package com.lovetropics.minigames.common.minigames.behaviours;

import com.google.gson.JsonElement;
import com.mojang.datafixers.Dynamic;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.function.Function;

public interface IMinigameBehaviorType extends IForgeRegistryEntry<IMinigameBehaviorType>
{
	ResourceLocation getID();
	Function<Dynamic<JsonElement>, IMinigameBehavior> getInstanceFactory();
}
