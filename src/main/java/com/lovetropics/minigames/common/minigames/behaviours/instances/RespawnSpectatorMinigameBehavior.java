package com.lovetropics.minigames.common.minigames.behaviours.instances;

import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.MinigameManager;
import com.lovetropics.minigames.common.minigames.PlayerRole;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.mojang.serialization.Codec;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.GameRules;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public final class RespawnSpectatorMinigameBehavior implements IMinigameBehavior {
	public static final Codec<RespawnSpectatorMinigameBehavior> CODEC = Codec.unit(RespawnSpectatorMinigameBehavior::new);

	@Override
	public void onPlayerChangeRole(IMinigameInstance minigame, ServerPlayerEntity player, PlayerRole role, PlayerRole lastRole) {
		if (minigame.getParticipants().isEmpty()) {
			MinigameManager.getInstance().finish();
		}
	}

	@Override
	public void onPlayerDeath(IMinigameInstance minigame, ServerPlayerEntity player, LivingDeathEvent event) {
		player.inventory.dropAllItems();

		if (!minigame.getSpectators().contains(player.getUniqueID())) {
			minigame.addPlayer(player, PlayerRole.SPECTATOR);
			player.setHealth(20.0F);
			event.setCanceled(true);

			sendDeathMessage(minigame, player);
		}
	}

	private void sendDeathMessage(IMinigameInstance minigame, ServerPlayerEntity player) {
		ServerWorld world = minigame.getWorld();
		if (!world.getGameRules().getBoolean(GameRules.SHOW_DEATH_MESSAGES)) {
			return;
		}

		ITextComponent message = player.getCombatTracker().getDeathMessage();
		minigame.getPlayers().sendMessage(message);
	}
}
