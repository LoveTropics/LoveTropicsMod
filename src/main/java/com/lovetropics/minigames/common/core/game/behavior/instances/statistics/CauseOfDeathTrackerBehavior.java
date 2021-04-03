package com.lovetropics.minigames.common.core.game.behavior.instances.statistics;

import com.lovetropics.minigames.common.core.game.IActiveGame;
import com.lovetropics.minigames.common.core.game.PlayerRole;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.statistics.CauseOfDeath;
import com.lovetropics.minigames.common.core.game.statistics.GameStatistics;
import com.lovetropics.minigames.common.core.game.statistics.StatisticKey;
import com.lovetropics.minigames.common.core.game.statistics.StatisticsMap;
import com.mojang.serialization.Codec;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;

public final class CauseOfDeathTrackerBehavior implements IGameBehavior {
	public static final Codec<CauseOfDeathTrackerBehavior> CODEC = Codec.unit(CauseOfDeathTrackerBehavior::new);

	@Override
	public void register(IActiveGame registerGame, EventRegistrar events) {
		events.listen(GamePlayerEvents.JOIN, this::onPlayerJoin);
		events.listen(GamePlayerEvents.CHANGE_ROLE, this::onPlayerChangeRole);
		events.listen(GamePlayerEvents.DEATH, this::onPlayerDeath);
	}

	private void onPlayerJoin(IActiveGame game, ServerPlayerEntity player, PlayerRole role) {
		if (role == PlayerRole.PARTICIPANT) {
			game.getStatistics().forPlayer(player).set(StatisticKey.DEAD, false);
		}
	}

	private void onPlayerChangeRole(IActiveGame game, ServerPlayerEntity player, PlayerRole role, PlayerRole lastRole) {
		if (lastRole == PlayerRole.PARTICIPANT && role == PlayerRole.SPECTATOR) {
			game.getStatistics().forPlayer(player).set(StatisticKey.DEAD, true);
		}
	}

	private ActionResultType onPlayerDeath(IActiveGame game, ServerPlayerEntity player, DamageSource source) {
		GameStatistics statistics = game.getStatistics();
		StatisticsMap playerStatistics = statistics.forPlayer(player);

		playerStatistics.set(StatisticKey.CAUSE_OF_DEATH, CauseOfDeath.from(source));
		playerStatistics.set(StatisticKey.DEAD, true);

		return ActionResultType.PASS;
	}
}
