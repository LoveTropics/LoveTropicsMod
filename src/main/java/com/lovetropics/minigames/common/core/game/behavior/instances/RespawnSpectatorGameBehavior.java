package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.minigames.common.core.game.GameManager;
import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.PlayerRole;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.mojang.serialization.Codec;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.GameRules;
import net.minecraft.world.server.ServerWorld;

public final class RespawnSpectatorGameBehavior implements IGameBehavior {
	public static final Codec<RespawnSpectatorGameBehavior> CODEC = Codec.unit(RespawnSpectatorGameBehavior::new);

	@Override
	public void register(IGameInstance registerGame, GameEventListeners events) {
		events.listen(GamePlayerEvents.CHANGE_ROLE, this::onPlayerChangeRole);
		events.listen(GamePlayerEvents.DEATH, this::onPlayerDeath);
	}

	private void onPlayerChangeRole(IGameInstance minigame, ServerPlayerEntity player, PlayerRole role, PlayerRole lastRole) {
		if (minigame.getParticipants().isEmpty()) {
			GameManager.get().finish();
		}
	}

	private ActionResultType onPlayerDeath(IGameInstance minigame, ServerPlayerEntity player, DamageSource source) {
		player.inventory.dropAllItems();

		if (!minigame.getSpectators().contains(player.getUniqueID())) {
			minigame.addPlayer(player, PlayerRole.SPECTATOR);
			player.setHealth(20.0F);

			sendDeathMessage(minigame, player);

			return ActionResultType.FAIL;
		}

		return ActionResultType.PASS;
	}

	private void sendDeathMessage(IGameInstance minigame, ServerPlayerEntity player) {
		ServerWorld world = minigame.getWorld();
		if (!world.getGameRules().getBoolean(GameRules.SHOW_DEATH_MESSAGES)) {
			return;
		}

		ITextComponent message = player.getCombatTracker().getDeathMessage();
		minigame.getAllPlayers().sendMessage(message);
	}
}
