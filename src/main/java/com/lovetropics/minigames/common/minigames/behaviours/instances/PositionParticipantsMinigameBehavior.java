package com.lovetropics.minigames.common.minigames.behaviours.instances;

import com.lovetropics.minigames.common.minigames.IMinigameDefinition;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class PositionParticipantsMinigameBehavior implements IMinigameBehavior
{
	private final BlockPos[] startPositions;

	public PositionParticipantsMinigameBehavior(final BlockPos[] startPositions)
	{
		this.startPositions = startPositions;
	}

	@Override
	public void onPreStart(final IMinigameDefinition definition, MinecraftServer server)
	{

	}
}
