package com.lovetropics.minigames.common.minigames.behaviours;

import com.google.gson.JsonElement;
import com.mojang.datafixers.Dynamic;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.function.Function;

public class MinigameBehaviorType<T extends IMinigameBehavior> extends ForgeRegistryEntry<IMinigameBehaviorType<?>> implements IMinigameBehaviorType<T>
{
	private final Function<Dynamic<JsonElement>, T> instanceFactory;

	public MinigameBehaviorType(final ResourceLocation id, final Function<Dynamic<JsonElement>, T> instanceFactory)
	{
		this.instanceFactory = instanceFactory;
	}

	@Override
	public T create(Dynamic<JsonElement> data)
	{
		return instanceFactory.apply(data);
	}
}
