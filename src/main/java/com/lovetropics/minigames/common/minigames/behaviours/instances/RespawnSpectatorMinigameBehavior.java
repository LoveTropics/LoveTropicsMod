package com.lovetropics.minigames.common.minigames.behaviours.instances;

import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.MinigameManager;
import com.lovetropics.minigames.common.minigames.PlayerRole;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public final class RespawnSpectatorMinigameBehavior implements IMinigameBehavior {
	public static final RespawnSpectatorMinigameBehavior INSTANCE = new RespawnSpectatorMinigameBehavior();

	private RespawnSpectatorMinigameBehavior() {
	}

	@Override
	public void onPlayerDeath(IMinigameInstance minigame, ServerPlayerEntity player, LivingDeathEvent event) {
		if (!minigame.getSpectators().contains(player.getUniqueID())) {
			minigame.addPlayer(player, PlayerRole.SPECTATOR);
			player.setHealth(20.0F);
			event.setCanceled(true);
		}

		if (minigame.getParticipants().isEmpty()) {
			MinigameManager.getInstance().finish();
		}

		player.inventory.dropAllItems();
	}
}
