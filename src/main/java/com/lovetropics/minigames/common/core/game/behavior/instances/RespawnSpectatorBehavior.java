package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.minigames.common.core.game.GameStopReason;
import com.lovetropics.minigames.common.core.game.IActiveGame;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
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

public final class RespawnSpectatorBehavior implements IGameBehavior {
	public static final Codec<RespawnSpectatorBehavior> CODEC = Codec.unit(RespawnSpectatorBehavior::new);

	@Override
	public void register(IActiveGame registerGame, EventRegistrar events) {
		events.listen(GamePlayerEvents.CHANGE_ROLE, this::onPlayerChangeRole);
		events.listen(GamePlayerEvents.DEATH, this::onPlayerDeath);
	}

	private void onPlayerChangeRole(IActiveGame game, ServerPlayerEntity player, PlayerRole role, PlayerRole lastRole) {
		if (game.getParticipants().isEmpty()) {
			game.stop(GameStopReason.FINISHED);
		}
	}

	private ActionResultType onPlayerDeath(IActiveGame game, ServerPlayerEntity player, DamageSource source) {
		player.inventory.dropAllItems();

		if (!game.getSpectators().contains(player)) {
			game.setPlayerRole(player, PlayerRole.SPECTATOR);
			player.setHealth(20.0F);

			sendDeathMessage(game, player);

			return ActionResultType.FAIL;
		}

		return ActionResultType.PASS;
	}

	private void sendDeathMessage(IActiveGame game, ServerPlayerEntity player) {
		ServerWorld world = game.getWorld();
		if (!world.getGameRules().getBoolean(GameRules.SHOW_DEATH_MESSAGES)) {
			return;
		}

		ITextComponent message = player.getCombatTracker().getDeathMessage();
		game.getAllPlayers().sendMessage(message);
	}
}
