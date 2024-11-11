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
import com.lovetropics.minigames.common.util.Util;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;

public record ImmediateRespawnBehavior(Optional<PlayerRole> role, Optional<PlayerRole> respawnAsRole, Optional<TemplatedText> deathMessage, boolean dropInventory, GameActionList<ServerPlayer> respawnAction, boolean spectateKiller) implements IGameBehavior {
	public static final MapCodec<ImmediateRespawnBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			PlayerRole.CODEC.optionalFieldOf("role").forGetter(c -> c.role),
			PlayerRole.CODEC.optionalFieldOf("respawn_as").forGetter(c -> c.respawnAsRole),
			TemplatedText.CODEC.optionalFieldOf("death_message").forGetter(c -> c.deathMessage),
			Codec.BOOL.optionalFieldOf("drop_inventory", false).forGetter(c -> c.dropInventory),
			GameActionList.PLAYER_CODEC.optionalFieldOf("respawn_action", GameActionList.EMPTY).forGetter(c -> c.respawnAction),
			Codec.BOOL.optionalFieldOf("spectate_killer", true).forGetter(c -> c.spectateKiller)
	).apply(i, ImmediateRespawnBehavior::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		respawnAction.register(game, events);
		events.listen(GamePlayerEvents.DEATH, (player, source) -> onPlayerDeath(game, player, source));
	}

	private InteractionResult onPlayerDeath(IGamePhase game, ServerPlayer player, DamageSource source) {
		destroyVanishingCursedItems(player.getInventory());
		if (dropInventory) {
			player.getInventory().dropAll();
		}

		PlayerRole playerRole = game.getRoleFor(player);
		if (role.isEmpty() || role.get() == playerRole) {
			respawnPlayer(game, player, playerRole, source);
			sendDeathMessage(game, player);

			return InteractionResult.FAIL;
		}

		return InteractionResult.PASS;
	}

	private void respawnPlayer(IGamePhase game, ServerPlayer player, @Nullable PlayerRole playerRole, DamageSource source) {
		if (respawnAsRole.isPresent()) {
			game.setPlayerRole(player, respawnAsRole.get());
			final ServerPlayer killer = Util.getKillerPlayer(player, source);
			if (spectateKiller && respawnAsRole.get() == PlayerRole.SPECTATOR && killer != null) {
				player.setCamera(killer);
			}
		} else {
			SpawnBuilder spawn = new SpawnBuilder(player);
			game.invoker(GamePlayerEvents.SPAWN).onSpawn(player.getUUID(), spawn, playerRole);
			spawn.teleportAndApply(player);
		}

		player.setHealth(20.0F);
		player.setDeltaMovement(0, 0, 0);
		player.fallDistance = 0.0f;

		respawnAction.apply(game, GameActionContext.EMPTY, player);
	}

	private void sendDeathMessage(IGamePhase game, ServerPlayer player) {
		if (deathMessage.isPresent()) {
			Component message = deathMessage.get().apply(Map.of("message", player.getCombatTracker().getDeathMessage()));
			game.allPlayers().sendMessage(message);
		}
	}

	public static void destroyVanishingCursedItems(Container inventory) {
		for (int i = 0; i < inventory.getContainerSize(); ++i) {
			if (EnchantmentHelper.has(inventory.getItem(i), EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)) {
				inventory.removeItemNoUpdate(i);
			}
		}
	}
}
