package com.lovetropics.minigames.common.core.game.state.statistics;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import net.minecraft.network.chat.Component;

public interface StatisticHolder {
	Component getName(IGamePhase game);

	StatisticsMap getOwnStatistics(GameStatistics statistics);
}
