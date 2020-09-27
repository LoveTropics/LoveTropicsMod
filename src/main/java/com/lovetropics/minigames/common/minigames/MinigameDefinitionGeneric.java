package com.lovetropics.minigames.common.minigames;

import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehaviorType;
import com.lovetropics.minigames.common.minigames.config.MinigameConfig;
import com.lovetropics.minigames.common.minigames.map.IMinigameMapProvider;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;
import java.util.Optional;

public class MinigameDefinitionGeneric implements IMinigameDefinition
{
	private final MinigameConfig config;

	public MinigameDefinitionGeneric(final MinigameConfig config) {
		this.config = config;
	}

	@Override
	public IMinigameMapProvider getMapProvider() {
		return config.mapProvider;
	}

	@Override
	public Collection<IMinigameBehavior> getAllBehaviours()
	{
		return config.behaviors.values();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends IMinigameBehavior> Optional<T> getBehavior(IMinigameBehaviorType<T> type)
	{
		return Optional.ofNullable((T) config.behaviors.get(type));
	}

	@Override
	public ResourceLocation getID()
	{
		return config.id;
	}

	@Override
	public String getUnlocalizedName()
	{
		return config.translationKey;
	}

	@Override
	public int getMinimumParticipantCount()
	{
		return config.minimumParticipants;
	}

	@Override
	public int getMaximumParticipantCount()
	{
		return config.maximumParticipants;
	}
}
