package com.lovetropics.minigames.common.core.game.behavior.instances.tweak;

import com.lovetropics.minigames.common.core.game.IActiveGame;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLifecycleEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.state.instances.TeamKey;
import com.lovetropics.minigames.common.core.game.state.instances.TeamState;
import com.lovetropics.minigames.common.util.MoreCodecs;
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
	public void register(IActiveGame registerGame, EventRegistrar events) {
		events.listen(GameLifecycleEvents.START, this::onStart);
		events.listen(GamePlayerEvents.LEAVE, this::onPlayerLeave);
	}

	private void onStart(IActiveGame game) {
		for (ServerPlayerEntity player : game.getParticipants()) {
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
	}

	private double getMaxHealthForPlayer(IActiveGame game, ServerPlayerEntity player) {
		TeamKey team = getTeamOrNull(game, player);
		if (team != null) {
			return maxHealthByTeam.getOrDefault(team.key, 20.0);
		}
		return maxHealth;
	}

	private void onPlayerLeave(IActiveGame game, ServerPlayerEntity player) {
		player.getAttribute(Attributes.MAX_HEALTH).removeModifier(ATTRIBUTE_ID);
	}

	@Nullable
	private TeamKey getTeamOrNull(IActiveGame game, ServerPlayerEntity player) {
		TeamState teamState = game.getState().getOrNull(TeamState.TYPE);
		if (teamState != null) {
			return teamState.getTeamForPlayer(player);
		} else {
			return null;
		}
	}
}
