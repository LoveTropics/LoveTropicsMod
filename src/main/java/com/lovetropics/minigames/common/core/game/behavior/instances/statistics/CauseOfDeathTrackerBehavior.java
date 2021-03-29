package com.lovetropics.minigames.common.core.game.behavior.instances.statistics;

import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.PlayerRole;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.statistics.CauseOfDeath;
import com.lovetropics.minigames.common.core.game.statistics.MinigameStatistics;
import com.lovetropics.minigames.common.core.game.statistics.StatisticKey;
import com.lovetropics.minigames.common.core.game.statistics.StatisticsMap;
import com.mojang.serialization.Codec;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public final class CauseOfDeathTrackerBehavior implements IGameBehavior {
	public static final Codec<CauseOfDeathTrackerBehavior> CODEC = Codec.unit(CauseOfDeathTrackerBehavior::new);

	@Override
	public void onPlayerJoin(IGameInstance minigame, ServerPlayerEntity player, PlayerRole role) {
		if (role == PlayerRole.PARTICIPANT) {
			minigame.getStatistics().forPlayer(player).set(StatisticKey.DEAD, false);
		}
	}

	@Override
	public void onPlayerChangeRole(IGameInstance minigame, ServerPlayerEntity player, PlayerRole role, PlayerRole lastRole) {
		if (lastRole == PlayerRole.PARTICIPANT && role == PlayerRole.SPECTATOR) {
			minigame.getStatistics().forPlayer(player).set(StatisticKey.DEAD, true);
		}
	}

	@Override
	public void onPlayerDeath(IGameInstance minigame, ServerPlayerEntity player, LivingDeathEvent event) {
		MinigameStatistics statistics = minigame.getStatistics();
		StatisticsMap playerStatistics = statistics.forPlayer(player);

		playerStatistics.set(StatisticKey.CAUSE_OF_DEATH, CauseOfDeath.from(event.getSource()));
		playerStatistics.set(StatisticKey.DEAD, true);
	}
}
