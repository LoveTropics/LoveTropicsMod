package com.lovetropics.minigames.common.minigames.behaviours.instances;

import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.MinigameBehaviorTypes;
import com.mojang.datafixers.Dynamic;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.ServerPlayerEntity;

import javax.annotation.Nullable;
import java.util.UUID;

public final class SetMaxHealthBehavior implements IMinigameBehavior {
	private static final UUID ATTRIBUTE_ID = UUID.fromString("3e226aa5-fbcd-495e-af62-9af714b204b6");
	private static final String ATTRIBUTE_NAME = "minigame_max_health";

	private final double maxHealth;
	private final Object2DoubleMap<String> maxHealthByTeam;

	public SetMaxHealthBehavior(double maxHealth, Object2DoubleMap<String> maxHealthByTeam) {
		this.maxHealth = maxHealth;
		this.maxHealthByTeam = maxHealthByTeam;
	}

	public static <T> SetMaxHealthBehavior parse(Dynamic<T> root) {
		double maxHealth = root.get("max_health").asDouble(20.0);
		Object2DoubleOpenHashMap<String> maxHealthByTeam = new Object2DoubleOpenHashMap<>(root.get("max_health_by_team").asMap(
				key -> key.asString(""),
				value -> value.asDouble(20.0)
		));

		return new SetMaxHealthBehavior(maxHealth, maxHealthByTeam);
	}

	@Override
	public void onStart(IMinigameInstance minigame) {
		for (ServerPlayerEntity player : minigame.getParticipants()) {
			double maxHealth = getMaxHealthForPlayer(minigame, player);
			if (maxHealth != 20.0) {
				player.getAttribute(SharedMonsterAttributes.MAX_HEALTH).applyModifier(
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

	private double getMaxHealthForPlayer(IMinigameInstance minigame, ServerPlayerEntity player) {
		TeamsBehavior.TeamKey team = getTeamOrNull(minigame, player);
		if (team != null) {
			return maxHealthByTeam.getOrDefault(team.key, 20.0);
		}
		return maxHealth;
	}

	@Override
	public void onPlayerLeave(IMinigameInstance minigame, ServerPlayerEntity player) {
		player.getAttribute(SharedMonsterAttributes.MAX_HEALTH).removeModifier(ATTRIBUTE_ID);
	}

	@Nullable
	private TeamsBehavior.TeamKey getTeamOrNull(IMinigameInstance minigame, ServerPlayerEntity player) {
		return minigame.getOneBehavior(MinigameBehaviorTypes.TEAMS.get())
				.map(teams -> teams.getTeamForPlayer(player))
				.orElse(null);
	}
}
