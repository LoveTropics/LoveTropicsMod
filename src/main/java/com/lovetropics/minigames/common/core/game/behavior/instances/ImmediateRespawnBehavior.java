package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.SpawnBuilder;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionContext;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionList;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.util.TemplatedText;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;

import java.util.Map;
import java.util.Optional;

public record ImmediateRespawnBehavior(Optional<PlayerRole> role, Optional<PlayerRole> respawnAsRole, Optional<TemplatedText> deathMessage, boolean dropInventory, GameActionList<ServerPlayer> respawnAction) implements IGameBehavior {
	public static final MapCodec<ImmediateRespawnBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			PlayerRole.CODEC.optionalFieldOf("role").forGetter(c -> c.role),
			PlayerRole.CODEC.optionalFieldOf("respawn_as").forGetter(c -> c.respawnAsRole),
			TemplatedText.CODEC.optionalFieldOf("death_message").forGetter(c -> c.deathMessage),
			Codec.BOOL.optionalFieldOf("drop_inventory", false).forGetter(c -> c.dropInventory),
			GameActionList.PLAYER_CODEC.optionalFieldOf("respawn_action", GameActionList.EMPTY).forGetter(c -> c.respawnAction)
	).apply(i, ImmediateRespawnBehavior::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		respawnAction.register(game, events);
		events.listen(GamePlayerEvents.DEATH, (player, source) -> onPlayerDeath(game, player));
	}

	private InteractionResult onPlayerDeath(IGamePhase game, ServerPlayer player) {
		if (dropInventory) {
			player.getInventory().dropAll();
		}

		PlayerRole playerRole = game.getRoleFor(player);
		if (this.role.isEmpty() || this.role.get() == playerRole) {
			this.respawnPlayer(game, player, playerRole);
			this.sendDeathMessage(game, player);

			return InteractionResult.FAIL;
		}

		return InteractionResult.PASS;
	}

	private void respawnPlayer(IGamePhase game, ServerPlayer player, PlayerRole playerRole) {
		if (respawnAsRole.isPresent()) {
			game.setPlayerRole(player, respawnAsRole.get());
		} else {
			SpawnBuilder spawn = new SpawnBuilder(player);
			game.invoker(GamePlayerEvents.SPAWN).onSpawn(player.getUUID(), spawn, playerRole);
			spawn.teleportAndApply(player);
		}

		player.setHealth(20.0F);

		respawnAction.apply(game, GameActionContext.EMPTY, player);
	}

	private void sendDeathMessage(IGamePhase game, ServerPlayer player) {
		if (deathMessage.isPresent()) {
			Component message = deathMessage.get().apply(Map.of("message", player.getCombatTracker().getDeathMessage()));
			game.getAllPlayers().sendMessage(message);
		}
	}
}
