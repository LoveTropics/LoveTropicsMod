package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.state.statistics.PlacementOrder;
import com.lovetropics.minigames.common.core.game.state.statistics.PlayerKey;
import com.lovetropics.minigames.common.core.game.state.statistics.PlayerPlacement;
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticKey;
import com.lovetropics.minigames.common.core.game.util.GameSidebar;
import com.lovetropics.minigames.common.core.game.util.GlobalGameWidgets;
import com.lovetropics.minigames.common.core.game.util.TemplatedText;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record PointsSidebarBehavior(
		StatisticKey<Integer> statistic,
		PlacementOrder order,
		int count,
		Component title,
		List<TemplatedText> header
) implements IGameBehavior {
	public static final MapCodec<PointsSidebarBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			StatisticKey.INT_CODEC.fieldOf("statistic").forGetter(PointsSidebarBehavior::statistic),
			PlacementOrder.CODEC.optionalFieldOf("order", PlacementOrder.MAX).forGetter(PointsSidebarBehavior::order),
			Codec.INT.optionalFieldOf("count", 5).forGetter(PointsSidebarBehavior::count),
			ExtraCodecs.COMPONENT.fieldOf("title").forGetter(PointsSidebarBehavior::title),
			TemplatedText.CODEC.listOf().fieldOf("header").forGetter(PointsSidebarBehavior::header)
	).apply(i, PointsSidebarBehavior::new));

	private static final int REFRESH_INTERVAL = SharedConstants.TICKS_PER_SECOND / 2;

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		final GameSidebar sidebar = GlobalGameWidgets.registerTo(game, events).openSidebar(title);
		events.listen(GamePhaseEvents.TICK, () -> {
			if (game.ticks() % REFRESH_INTERVAL == 0) {
				sidebar.set(renderSidebar(game));
			}
		});
	}

	private Component[] renderSidebar(final IGamePhase game) {
		int totalCount = 0;
		for (final PlayerKey player : game.getStatistics().getPlayers()) {
			totalCount += game.getStatistics().forPlayer(player).getOr(statistic, 0);
		}

		final List<Component> sidebar = new ArrayList<>(10);
		for (final TemplatedText line : header) {
			sidebar.add(line.apply(Map.of("total", Component.literal(String.valueOf(totalCount)))));
		}

		final PlayerPlacement.Score<Integer> placement = PlayerPlacement.fromMaxScore(game, statistic, false);
		placement.addToSidebar(sidebar, count);

		return sidebar.toArray(new Component[0]);
	}
}
