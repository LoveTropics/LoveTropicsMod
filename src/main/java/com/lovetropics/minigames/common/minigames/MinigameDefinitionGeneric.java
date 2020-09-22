package com.lovetropics.minigames.common.minigames;

import java.util.Collection;
import java.util.Optional;

import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehaviorType;
import com.lovetropics.minigames.common.minigames.config.MinigameConfig;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class MinigameDefinitionGeneric extends ForgeRegistryEntry<IMinigameDefinition> implements IMinigameDefinition
{
	private final MinigameConfig config;

	public MinigameDefinitionGeneric(final MinigameConfig config) {
		this.config = config;
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
	public DimensionType getDimension()
	{
		return config.dimension;
	}

	@Override
	public GameType getParticipantGameType()
	{
		return config.participantGameType;
	}

	@Override
	public GameType getSpectatorGameType()
	{
		return config.spectatorGameType;
	}

	@Override
	public BlockPos getSpectatorPosition()
	{
		return config.spectatorPosition;
	}

	@Override
	public BlockPos getPlayerRespawnPosition(IMinigameInstance instance)
	{
		return config.respawnPosition;
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