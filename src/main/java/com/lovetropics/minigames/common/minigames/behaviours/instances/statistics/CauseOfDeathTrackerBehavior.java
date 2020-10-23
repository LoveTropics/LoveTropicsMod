package com.lovetropics.minigames.common.minigames.behaviours.instances.statistics;

import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.lovetropics.minigames.common.minigames.statistics.CauseOfDeath;
import com.lovetropics.minigames.common.minigames.statistics.MinigameStatistics;
import com.lovetropics.minigames.common.minigames.statistics.StatisticKey;
import com.lovetropics.minigames.common.minigames.statistics.StatisticsMap;
import com.mojang.datafixers.Dynamic;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public final class CauseOfDeathTrackerBehavior implements IMinigameBehavior {
	public static <T> CauseOfDeathTrackerBehavior parse(Dynamic<T> root) {
		return new CauseOfDeathTrackerBehavior();
	}

	@Override
	public void onPlayerDeath(IMinigameInstance minigame, ServerPlayerEntity player, LivingDeathEvent event) {
		MinigameStatistics statistics = minigame.getStatistics();
		StatisticsMap playerStatistics = statistics.forPlayer(player);

		playerStatistics.set(StatisticKey.CAUSE_OF_DEATH, CauseOfDeath.from(event.getSource()));
	}
}
