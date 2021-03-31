package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.IGameManager;
import com.lovetropics.minigames.common.core.game.PlayerRole;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
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
	public void register(IGameInstance registerGame, EventRegistrar events) {
		events.listen(GamePlayerEvents.CHANGE_ROLE, this::onPlayerChangeRole);
		events.listen(GamePlayerEvents.DEATH, this::onPlayerDeath);
	}

	private void onPlayerChangeRole(IGameInstance game, ServerPlayerEntity player, PlayerRole role, PlayerRole lastRole) {
		if (game.getParticipants().isEmpty()) {
			IGameManager.get().finish(game);
		}
	}

	private ActionResultType onPlayerDeath(IGameInstance game, ServerPlayerEntity player, DamageSource source) {
		player.inventory.dropAllItems();

		if (!game.getSpectators().contains(player.getUniqueID())) {
			game.addPlayer(player, PlayerRole.SPECTATOR);
			player.setHealth(20.0F);

			sendDeathMessage(game, player);

			return ActionResultType.FAIL;
		}

		return ActionResultType.PASS;
	}

	private void sendDeathMessage(IGameInstance game, ServerPlayerEntity player) {
		ServerWorld world = game.getWorld();
		if (!world.getGameRules().getBoolean(GameRules.SHOW_DEATH_MESSAGES)) {
			return;
		}

		ITextComponent message = player.getCombatTracker().getDeathMessage();
		game.getAllPlayers().sendMessage(message);
	}
}
