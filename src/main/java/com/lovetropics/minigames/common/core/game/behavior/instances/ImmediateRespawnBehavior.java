package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.util.TemplatedText;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.text.ITextComponent;

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

	private ActionResultType onPlayerDeath(IGamePhase game, ServerPlayerEntity player) {
		player.inventory.dropAllItems();

		if (this.role == null || this.role == game.getRoleFor(player)) {
			if (this.respawnAsRole != null) {
				game.setPlayerRole(player, this.respawnAsRole);
			}

			player.setHealth(20.0F);

			this.sendDeathMessage(game, player);

			return ActionResultType.FAIL;
		}

		return ActionResultType.PASS;
	}

	private void sendDeathMessage(IGamePhase game, ServerPlayerEntity player) {
		if (deathMessage != null) {
			ITextComponent message = deathMessage.apply(player.getCombatTracker().getDeathMessage());
			game.getAllPlayers().sendMessage(message);
		}
	}
}
