package com.lovetropics.minigames.common.minigames.behaviours.instances;

import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.MinigameManager;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import net.minecraft.entity.player.ServerPlayerEntity;

public final class RespawnSpectatorMinigameBehavior implements IMinigameBehavior {
	public static final RespawnSpectatorMinigameBehavior INSTANCE = new RespawnSpectatorMinigameBehavior();

	private RespawnSpectatorMinigameBehavior() {
	}

	@Override
	public void onPlayerDeath(IMinigameInstance minigame, ServerPlayerEntity player) {
		if (!minigame.getSpectators().contains(player.getUniqueID())) {
			minigame.removeParticipant(player);
			minigame.addSpectator(player);

			player.setGameType(minigame.getDefinition().getSpectatorGameType());
		}

		if (minigame.getParticipants().isEmpty()) {
			MinigameManager.getInstance().finishCurrentMinigame();
		}

		player.inventory.dropAllItems();
	}
}
