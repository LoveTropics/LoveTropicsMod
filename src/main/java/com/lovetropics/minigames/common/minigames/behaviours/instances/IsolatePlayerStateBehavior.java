package com.lovetropics.minigames.common.minigames.behaviours.instances;

import com.google.common.collect.Maps;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.MinigamePlayerCache;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.Map;
import java.util.UUID;

public final class IsolatePlayerStateBehavior implements IMinigameBehavior {
	/**
	 * Cache used to know what state the player was in before teleporting into a minigame.
	 */
	private final Map<UUID, MinigamePlayerCache> playerCache = Maps.newHashMap();

	@Override
	public void onAddPlayer(IMinigameInstance minigame, ServerPlayerEntity player) {
		MinigamePlayerCache playerCache = new MinigamePlayerCache(player);
		playerCache.resetPlayerStats(player);

		this.playerCache.put(player.getUniqueID(), playerCache);
	}

	@Override
	public void onRemovePlayer(IMinigameInstance minigame, ServerPlayerEntity player) {
		// try to restore the player to their old state
		MinigamePlayerCache playerCache = this.playerCache.remove(player.getUniqueID());
		if (playerCache != null) {
			playerCache.teleportBack(player);
		}
	}
}
