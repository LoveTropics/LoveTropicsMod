package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.util.TemplatedText;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.Optional;

public final class ImmediateRespawnBehavior implements IGameBehavior {
	public static final Codec<ImmediateRespawnBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				PlayerRole.CODEC.optionalFieldOf("role").forGetter(c -> Optional.ofNullable(c.role)),
				PlayerRole.CODEC.optionalFieldOf("respawn_as").forGetter(c -> Optional.ofNullable(c.respawnAsRole)),
				TemplatedText.CODEC.optionalFieldOf("death_message").forGetter(c -> Optional.ofNullable(c.deathMessage)),
				Codec.BOOL.optionalFieldOf("drop_inventory", false).forGetter(c -> c.dropInventory)
		).apply(instance, ImmediateRespawnBehavior::new);
	});

	@Nullable
	private final PlayerRole role;
	@Nullable
	private final PlayerRole respawnAsRole;
	@Nullable
	private final TemplatedText deathMessage;
	private final boolean dropInventory;

	public ImmediateRespawnBehavior(Optional<PlayerRole> role, Optional<PlayerRole> respawnAsRole, Optional<TemplatedText> deathMessage, boolean dropInventory) {
		this.role = role.orElse(null);
		this.respawnAsRole = respawnAsRole.orElse(null);
		this.deathMessage = deathMessage.orElse(null);
		this.dropInventory = dropInventory;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GamePlayerEvents.DEATH, (player, source) -> onPlayerDeath(game, player));
	}

	private InteractionResult onPlayerDeath(IGamePhase game, ServerPlayer player) {
		player.inventory.dropAll();

		PlayerRole playerRole = game.getRoleFor(player);
		if (this.role == null || this.role == playerRole) {
			this.respawnPlayer(game, player, playerRole);
			this.sendDeathMessage(game, player);

			return InteractionResult.FAIL;
		}

		return InteractionResult.PASS;
	}

	private void respawnPlayer(IGamePhase game, ServerPlayer player, PlayerRole playerRole) {
		if (respawnAsRole != null) {
			game.setPlayerRole(player, respawnAsRole);
		} else {
			game.invoker(GamePlayerEvents.SPAWN).onSpawn(player, playerRole);
		}

		player.setHealth(20.0F);
	}

	private void sendDeathMessage(IGamePhase game, ServerPlayer player) {
		if (deathMessage != null) {
			Component message = deathMessage.apply(player.getCombatTracker().getDeathMessage());
			game.getAllPlayers().sendMessage(message);
		}
	}
}
