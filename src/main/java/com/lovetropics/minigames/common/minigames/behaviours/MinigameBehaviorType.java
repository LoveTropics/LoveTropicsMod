package com.lovetropics.minigames.common.minigames.behaviours;

import com.google.gson.JsonElement;
import com.mojang.datafixers.Dynamic;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.function.Function;

public class MinigameBehaviorType extends ForgeRegistryEntry<IMinigameBehaviorType> implements IMinigameBehaviorType
{
	private final ResourceLocation id;
	private final Function<Dynamic<JsonElement>, IMinigameBehavior> instanceFactory;

	public MinigameBehaviorType(final ResourceLocation id, final Function<Dynamic<JsonElement>, IMinigameBehavior> instanceFactory)
	{
		this.id = id;
		this.instanceFactory = instanceFactory;
	}

	@Override
	public ResourceLocation getID()
	{
		return id;
	}

	@Override
	public Function<Dynamic<JsonElement>, IMinigameBehavior> getInstanceFactory()
	{
		return instanceFactory;
	}
}
