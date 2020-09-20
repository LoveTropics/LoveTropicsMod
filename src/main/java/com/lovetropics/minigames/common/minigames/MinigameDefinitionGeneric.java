package com.lovetropics.minigames.common.minigames;

import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.lovetropics.minigames.common.minigames.config.MinigameConfig;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.List;

public class MinigameDefinitionGeneric extends ForgeRegistryEntry<IMinigameDefinition> implements IMinigameDefinition
{
	private final MinigameConfig config;

	public MinigameDefinitionGeneric(final MinigameConfig config) {
		this.config = config;
	}

	@Override
	public List<IMinigameBehavior> getBehaviours()
	{
		return config.behaviors;
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
