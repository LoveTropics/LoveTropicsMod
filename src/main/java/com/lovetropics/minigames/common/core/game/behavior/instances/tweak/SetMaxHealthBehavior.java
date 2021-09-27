package com.lovetropics.minigames.common.core.game.behavior.instances.tweak;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.state.team.TeamKey;
import com.lovetropics.minigames.common.core.game.state.team.TeamState;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.ServerPlayerEntity;

import javax.annotation.Nullable;
import java.util.UUID;

public final class SetMaxHealthBehavior implements IGameBehavior {
	public static final Codec<SetMaxHealthBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.DOUBLE.optionalFieldOf("max_health", 20.0).forGetter(c -> c.maxHealth),
				MoreCodecs.object2Double(Codec.STRING).fieldOf("max_health_by_team").orElseGet(Object2DoubleOpenHashMap::new).forGetter(c -> c.maxHealthByTeam)
		).apply(instance, SetMaxHealthBehavior::new);
	});

	private static final UUID ATTRIBUTE_ID = UUID.fromString("3e226aa5-fbcd-495e-af62-9af714b204b6");
	private static final String ATTRIBUTE_NAME = "minigame_max_health";

	private final double maxHealth;
	private final Object2DoubleMap<String> maxHealthByTeam;

	public SetMaxHealthBehavior(double maxHealth, Object2DoubleMap<String> maxHealthByTeam) {
		this.maxHealth = maxHealth;
		this.maxHealthByTeam = maxHealthByTeam;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GamePlayerEvents.SET_ROLE, (player, role, lastRole) -> {
			if (role == PlayerRole.PARTICIPANT) {
				applyToPlayer(game, player);
			} else if (lastRole == PlayerRole.PARTICIPANT) {
				removeFromPlayer(player);
			}
		});

		events.listen(GamePlayerEvents.LEAVE, this::removeFromPlayer);
	}

	private void applyToPlayer(IGamePhase game, ServerPlayerEntity player) {
		double maxHealth = getMaxHealthForPlayer(game, player);
		if (maxHealth != 20.0) {
			player.getAttribute(Attributes.MAX_HEALTH).applyNonPersistentModifier(
					new AttributeModifier(
							ATTRIBUTE_ID,
							ATTRIBUTE_NAME,
							maxHealth - 20.0,
							AttributeModifier.Operation.ADDITION
					)
			);
		}
	}

	private void removeFromPlayer(ServerPlayerEntity player) {
		player.getAttribute(Attributes.MAX_HEALTH).removeModifier(ATTRIBUTE_ID);
	}

	private double getMaxHealthForPlayer(IGamePhase game, ServerPlayerEntity player) {
		TeamKey team = getTeamOrNull(game, player);
		if (team != null) {
			return maxHealthByTeam.getOrDefault(team.key, 20.0);
		}
		return maxHealth;
	}

	@Nullable
	private TeamKey getTeamOrNull(IGamePhase game, ServerPlayerEntity player) {
		TeamState teamState = game.getState().getOrNull(TeamState.KEY);
		if (teamState != null) {
			return teamState.getTeamForPlayer(player);
		} else {
			return null;
		}
	}
}
