package com.lovetropics.minigames.common.minigames;

import com.lovetropics.minigames.common.minigames.behaviours.ConfiguredBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehaviorType;
import com.lovetropics.minigames.common.minigames.config.MinigameConfig;
import com.lovetropics.minigames.common.minigames.map.IMinigameMapProvider;
import it.unimi.dsi.fastutil.objects.Reference2ObjectLinkedOpenHashMap;
import net.minecraft.util.ResourceLocation;

import java.util.Map;

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
	public Map<IMinigameBehaviorType<?>, IMinigameBehavior> createBehaviors() {
		Map<IMinigameBehaviorType<?>, IMinigameBehavior> behaviors = new Reference2ObjectLinkedOpenHashMap<>();
		for (ConfiguredBehavior<?> behavior : config.behaviors) {
			behaviors.put(behavior.type, behavior.create());
		}
		return behaviors;
	}

	@Override
	public ResourceLocation getID()
	{
		return config.id;
	}

	@Override
	public String getUnlocalizedName()
	{
		return "ltminigames.minigame." + config.translationKey;
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
