package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.minigames.common.core.game.GameStopReason;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.mojang.serialization.Codec;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.GameRules;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;

public final class RespawnSpectatorBehavior implements IGameBehavior {
	public static final Codec<RespawnSpectatorBehavior> CODEC = Codec.unit(RespawnSpectatorBehavior::new);

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GamePlayerEvents.SET_ROLE, (player, role, lastRole) -> onPlayerSetRole(game, lastRole));
		events.listen(GamePlayerEvents.DEATH, (player, source) -> onPlayerDeath(game, player));
	}

	private void onPlayerSetRole(IGamePhase game, @Nullable PlayerRole lastRole) {
		if (lastRole == PlayerRole.PARTICIPANT && game.getParticipants().isEmpty()) {
			game.requestStop(GameStopReason.finished());
		}
	}

	private ActionResultType onPlayerDeath(IGamePhase game, ServerPlayerEntity player) {
		player.inventory.dropAllItems();

		if (game.getParticipants().contains(player)) {
			game.setPlayerRole(player, PlayerRole.SPECTATOR);
			player.setHealth(20.0F);

			sendDeathMessage(game, player);

			return ActionResultType.FAIL;
		}

		return ActionResultType.PASS;
	}

	private void sendDeathMessage(IGamePhase game, ServerPlayerEntity player) {
		ServerWorld world = game.getWorld();
		if (!world.getGameRules().getBoolean(GameRules.SHOW_DEATH_MESSAGES)) {
			return;
		}

		ITextComponent message = player.getCombatTracker().getDeathMessage();
		game.getAllPlayers().sendMessage(message);
	}
}
