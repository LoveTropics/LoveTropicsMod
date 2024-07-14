package com.lovetropics.minigames.common.core.game.state.statistics;

import com.google.common.collect.Iterators;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.player.PlayerSet;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public interface PlayerPlacement extends Iterable<Placed<PlayerKey>> {
	static Order fromDeathOrder(IGamePhase game, List<PlayerKey> deathOrder) {
		PlayerSet participants = game.participants();
		List<Placed<PlayerKey>> order = new ArrayList<>(participants.size() + deathOrder.size());

		boolean anyFirst = false;
		for (ServerPlayer player : participants) {
			order.add(Placed.at(1, PlayerKey.from(player)));
			anyFirst = true;
		}

		int placement = anyFirst ? 1 : 0;
		for (int i = deathOrder.size() - 1; i >= 0; i--) {
			order.add(Placed.at(++placement, deathOrder.get(i)));
		}

		return new Order(game, order);
	}

	static <T extends Comparable<T>> Score<T> fromMinScore(IGamePhase game, StatisticKey<T> score, boolean onlyOnline) {
		return fromScore(game, score, Comparator.naturalOrder(), onlyOnline);
	}

	static <T extends Comparable<T>> Score<T> fromMaxScore(IGamePhase game, StatisticKey<T> score, boolean onlyOnline) {
		return fromScore(game, score, Comparator.reverseOrder(), onlyOnline);
	}

	static <T extends Comparable<T>> Score<T> fromScore(PlacementOrder order, IGamePhase game, StatisticKey<T> statistic) {
		return fromScore(order, game, statistic, false);
	}

	static <T extends Comparable<T>> Score<T> fromScore(PlacementOrder order, IGamePhase game, StatisticKey<T> statistic, boolean onlyOnline) {
		if (order == PlacementOrder.MAX) {
			return fromMaxScore(game, statistic, onlyOnline);
		} else {
			return fromMinScore(game, statistic, onlyOnline);
		}
	}

	static <T> Score<T> fromScore(IGamePhase game, StatisticKey<T> scoreKey, Comparator<T> comparator, boolean onlyOnline) {
		GameStatistics statistics = game.statistics();

		List<PlayerKey> players = new ArrayList<>(statistics.getPlayers());
		if (onlyOnline) {
			players.removeIf(key -> !game.allPlayers().contains(key.id()));
		}
		players.sort(Comparator.comparing(
				player -> statistics.forPlayer(player).get(scoreKey),
				Comparator.nullsLast(comparator)
		));

		List<Score.Entry<T>> entries = new ArrayList<>(players.size());

		int placement = 0;
		T lastScore = null;

		for (PlayerKey player : players) {
			StatisticsMap playerStatistics = statistics.forPlayer(player);
			T score = playerStatistics.get(scoreKey);
			if (score == null) {
				continue;
			}

			if (!Objects.equals(score, lastScore) || placement == 0) placement++;
			lastScore = score;

			entries.add(new Score.Entry<>(player, placement, score));
		}

		return new Score<>(game, scoreKey, entries);
	}

	void placeInto(StatisticKey<Integer> placementKey);

	void sendTo(PlayerSet players, int length);

	void addToSidebar(List<Component> sidebar, int length);

	final class Order implements PlayerPlacement {
		private final IGamePhase game;
		private final List<Placed<PlayerKey>> order;

		Order(IGamePhase game, List<Placed<PlayerKey>> order) {
			this.game = game;
			this.order = order;
		}

		@Override
		public void placeInto(StatisticKey<Integer> placementKey) {
			GameStatistics statistics = game.statistics();
			statistics.clear(placementKey);

			for (Placed<PlayerKey> placed : order) {
				statistics.forPlayer(placed.value()).set(placementKey, placed.placement());
			}
		}

		@Override
		public void sendTo(PlayerSet players, int length) {
			int i = 0;
			Placed<PlayerKey> entry;

			for (int place = 1; place <= length; place++) {
				String headPrefix = " " + place + ". ";
				String indentPrefix = StringUtils.repeat(' ', headPrefix.length());

				boolean head = true;
				while (i < order.size() && (entry = order.get(i)).placement() == place) {
					String prefix = head ? headPrefix : indentPrefix;
					head = false;

					players.sendMessage(Component.literal(prefix).withStyle(ChatFormatting.AQUA)
							.append(Component.literal(entry.value().name()).withStyle(ChatFormatting.GOLD))
					);

					i++;
				}
			}
		}

		@Override
		public void addToSidebar(List<Component> sidebar, int length) {
			length = Math.min(order.size(), length);
			for (int i = 0; i < length; i++) {
				Placed<PlayerKey> entry = order.get(i);
				Component name = Component.literal(entry.value().name()).withStyle(ChatFormatting.AQUA);
				sidebar.add(Component.literal(" - ").append(name));
			}
		}

		@Override
		public Iterator<Placed<PlayerKey>> iterator() {
			return order.iterator();
		}
	}

	final class Score<T> implements PlayerPlacement {
		private final IGamePhase game;
		private final StatisticKey<T> scoreKey;
		private final List<Entry<T>> entries;

		Score(IGamePhase game, StatisticKey<T> scoreKey, List<Entry<T>> entries) {
			this.game = game;
			this.scoreKey = scoreKey;
			this.entries = entries;
		}

		@Override
		public void placeInto(StatisticKey<Integer> placementKey) {
			GameStatistics statistics = game.statistics();
			statistics.clear(placementKey);

			for (Entry<T> entry : entries) {
				statistics.forPlayer(entry.player).set(placementKey, entry.placement);
			}
		}

		@Override
		public void sendTo(PlayerSet players, int length) {
			int i = 0;
			Entry<T> entry;

			for (int place = 1; place <= length; place++) {
				String headPrefix = " " + place + ". ";
				String indentPrefix = StringUtils.repeat(' ', headPrefix.length());

				boolean head = true;
				while (i < entries.size() && (entry = entries.get(i)).placement == place) {
					String prefix = head ? headPrefix : indentPrefix;
					head = false;

					MutableComponent name = Component.literal(prefix + entry.player.name() + ": ");
					MutableComponent score = Component.literal(scoreKey.display(entry.score));

					players.sendMessage(name.withStyle(ChatFormatting.AQUA).append(score.withStyle(ChatFormatting.GOLD)));

					i++;
				}
			}
		}

		@Override
		public void addToSidebar(List<Component> sidebar, int length) {
			length = Math.min(entries.size(), length);
			for (int i = 0; i < length; i++) {
				Entry<T> entry = entries.get(i);
				Component name = Component.literal(entry.player.name() + ": ").withStyle(ChatFormatting.AQUA);
				Component score = Component.literal(scoreKey.display(entry.score)).withStyle(ChatFormatting.GOLD);
				sidebar.add(Component.literal(" - ").append(name).append(score));
			}
		}

		@Override
		public Iterator<Placed<PlayerKey>> iterator() {
			return Iterators.transform(entries.iterator(), input -> new Placed<>(input.placement, input.player));
		}

		record Entry<T>(PlayerKey player, int placement, T score) {
		}
	}
}
