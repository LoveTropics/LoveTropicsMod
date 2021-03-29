package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.GameManager;
import com.lovetropics.minigames.common.core.game.PlayerRole;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.mojang.serialization.Codec;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.GameRules;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public final class RespawnSpectatorGameBehavior implements IGameBehavior {
	public static final Codec<RespawnSpectatorGameBehavior> CODEC = Codec.unit(RespawnSpectatorGameBehavior::new);

	@Override
	public void onPlayerChangeRole(IGameInstance minigame, ServerPlayerEntity player, PlayerRole role, PlayerRole lastRole) {
		if (minigame.getParticipants().isEmpty()) {
			GameManager.getInstance().finish();
		}
	}

	@Override
	public void onPlayerDeath(IGameInstance minigame, ServerPlayerEntity player, LivingDeathEvent event) {
		player.inventory.dropAllItems();

		if (!minigame.getSpectators().contains(player.getUniqueID())) {
			minigame.addPlayer(player, PlayerRole.SPECTATOR);
			player.setHealth(20.0F);
			event.setCanceled(true);

			sendDeathMessage(minigame, player);
		}
	}

	private void sendDeathMessage(IGameInstance minigame, ServerPlayerEntity player) {
		ServerWorld world = minigame.getWorld();
		if (!world.getGameRules().getBoolean(GameRules.SHOW_DEATH_MESSAGES)) {
			return;
		}

		ITextComponent message = player.getCombatTracker().getDeathMessage();
		minigame.getPlayers().sendMessage(message);
	}
}
