package com.lovetropics.minigames.common.core.game.state.statistics;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.player.PlayerSet;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public interface PlayerPlacement {
	static Order fromDeathOrder(IGamePhase game, List<PlayerKey> deathOrder) {
		PlayerSet participants = game.getParticipants();
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

	static <T extends Comparable<T>> Score<T> fromMinScore(IGamePhase game, StatisticKey<T> score) {
		return fromScore(game, score, Comparator.naturalOrder());
	}

	static <T extends Comparable<T>> Score<T> fromMaxScore(IGamePhase game, StatisticKey<T> score) {
		return fromScore(game, score, Comparator.reverseOrder());
	}

	static <T> Score<T> fromScore(IGamePhase game, StatisticKey<T> scoreKey, Comparator<T> comparator) {
		GameStatistics statistics = game.getStatistics();

		List<PlayerKey> players = new ArrayList<>(statistics.getPlayers());
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
			GameStatistics statistics = game.getStatistics();
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
			GameStatistics statistics = game.getStatistics();
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

		public static class Entry<T> {
			public final PlayerKey player;
			public final int placement;
			public final T score;

			public Entry(PlayerKey player, int placement, T score) {
				this.player = player;
				this.placement = placement;
				this.score = score;
			}
		}
	}
}
