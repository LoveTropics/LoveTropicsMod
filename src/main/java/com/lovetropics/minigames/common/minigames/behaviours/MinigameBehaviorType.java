package com.lovetropics.minigames.common.minigames.behaviours;

import com.mojang.datafixers.Dynamic;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class MinigameBehaviorType<T> extends ForgeRegistryEntry<IMinigameBehaviorType<?>> implements IMinigameBehaviorType<T>
{
	private final Factory<T> instanceFactory;

	public MinigameBehaviorType(final Factory<T> instanceFactory)
	{
		this.instanceFactory = instanceFactory;
	}

	@Override
	public <D> T create(Dynamic<D> data)
	{
		return instanceFactory.create(data);
	}

	public interface Factory<T> {
		<D> T create(Dynamic<D> data);
	}
}
