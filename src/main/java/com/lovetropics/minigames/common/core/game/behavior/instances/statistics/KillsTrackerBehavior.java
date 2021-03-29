package com.lovetropics.minigames.common.core.game.behavior.instances.statistics;

import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.statistics.MinigameStatistics;
import com.lovetropics.minigames.common.core.game.statistics.PlayerKey;
import com.lovetropics.minigames.common.core.game.statistics.StatisticKey;
import com.lovetropics.minigames.common.core.game.statistics.StatisticsMap;
import com.mojang.serialization.Codec;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public final class KillsTrackerBehavior implements IGameBehavior {
	public static final Codec<KillsTrackerBehavior> CODEC = Codec.unit(KillsTrackerBehavior::new);

	@Override
	public void onPlayerDeath(IGameInstance minigame, ServerPlayerEntity player, LivingDeathEvent event) {
		MinigameStatistics statistics = minigame.getStatistics();
		StatisticsMap playerStatistics = statistics.forPlayer(player);

		Entity source = event.getSource().getTrueSource();
		if (source instanceof ServerPlayerEntity) {
			PlayerEntity killerPlayer = (PlayerEntity) source;

			statistics.forPlayer(killerPlayer)
					.withDefault(StatisticKey.KILLS, () -> 0)
					.apply(kills -> kills + 1);

			playerStatistics.set(StatisticKey.KILLED_BY, PlayerKey.from(killerPlayer));
		}
	}
}
