package com.lovetropics.minigames.common.game_actions;

import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.MinigameManager;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

public abstract class MinigameGameAction extends GameAction
{
	private static final Logger LOGGER = LogManager.getLogger(MinigameGameAction.class);

	public MinigameGameAction(UUID uuid, String triggerTime)
	{
		super(uuid, triggerTime);
	}

	@Override
	public final boolean resolve(MinecraftServer server)
	{
		try {
			final IMinigameInstance instance = MinigameManager.getInstance().getActiveMinigame();
			if (instance != null) {
				boolean resolved = false;
				for (IMinigameBehavior behavior : instance.getBehaviors()) {
					resolved |= notifyBehavior(instance, behavior);
				}
				return resolved;
			}
		} catch (Exception e) {
			LOGGER.error("Failed to apply minigame action", e);
		}
		return false;
	}

	public abstract boolean notifyBehavior(final IMinigameInstance instance, final IMinigameBehavior behavior);
}
