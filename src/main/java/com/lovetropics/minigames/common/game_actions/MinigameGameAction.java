package com.lovetropics.minigames.common.game_actions;

import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.MinigameManager;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import net.minecraft.server.MinecraftServer;

import java.util.UUID;

public abstract class MinigameGameAction extends GameAction
{
	public MinigameGameAction(UUID uuid, String triggerTime)
	{
		super(uuid, triggerTime);
	}

	@Override
	public final boolean resolve(MinecraftServer server)
	{
		final IMinigameInstance instance = MinigameManager.getInstance().getActiveMinigame();
		return instance != null && instance.getBehaviors().stream().anyMatch(behavior -> notifyBehavior(instance, behavior));
	}

	public abstract boolean notifyBehavior(final IMinigameInstance instance, final IMinigameBehavior behavior);
}
