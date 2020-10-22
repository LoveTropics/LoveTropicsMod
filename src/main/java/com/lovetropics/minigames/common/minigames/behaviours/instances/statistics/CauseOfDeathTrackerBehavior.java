package com.lovetropics.minigames.common.minigames.behaviours.instances.statistics;

import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.lovetropics.minigames.common.minigames.statistics.MinigameStatistics;
import com.lovetropics.minigames.common.minigames.statistics.StatisticKey;
import com.lovetropics.minigames.common.minigames.statistics.StatisticsMap;
import com.mojang.datafixers.Dynamic;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

import javax.annotation.Nullable;

public final class CauseOfDeathTrackerBehavior implements IMinigameBehavior {
	public static <T> CauseOfDeathTrackerBehavior parse(Dynamic<T> root) {
		return new CauseOfDeathTrackerBehavior();
	}

	@Override
	public void onPlayerDeath(IMinigameInstance minigame, ServerPlayerEntity player, LivingDeathEvent event) {
		MinigameStatistics statistics = minigame.getStatistics();
		StatisticsMap playerStatistics = statistics.forPlayer(player);

		Entity source = event.getSource().getTrueSource();
		if (source instanceof ServerPlayerEntity) {
			PlayerEntity killerPlayer = (PlayerEntity) source;
			playerStatistics.set(StatisticKey.CAUSE_OF_DEATH, "Killed by " + killerPlayer.getName().getString());
		} else {
			String causeOfDeath = displayCauseOfDeath(event.getSource());
			if (causeOfDeath != null) {
				playerStatistics.set(StatisticKey.CAUSE_OF_DEATH, causeOfDeath);
			}
		}
	}

	@Nullable
	private static String displayCauseOfDeath(DamageSource source) {
		if (source == DamageSource.DROWN) return "Drowned";
		else if (source == DamageSource.FALL) return "Hit the ground too hard";
		else if (source == DamageSource.ON_FIRE || source == DamageSource.IN_FIRE || source == DamageSource.LAVA) return "Burned";
		else if (source == DamageSource.STARVE) return "Starved";
		else if (source == DamageSource.LIGHTNING_BOLT) return "Struck by lightning";
		return null;
	}
}
