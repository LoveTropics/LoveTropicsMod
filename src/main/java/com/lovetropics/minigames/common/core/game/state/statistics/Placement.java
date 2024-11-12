package com.lovetropics.minigames.common.core.game.state.statistics;

import com.google.common.collect.Iterators;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.player.PlayerSet;
import com.lovetropics.minigames.common.core.game.state.team.GameTeamKey;
import com.lovetropics.minigames.common.core.game.state.team.TeamState;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public interface Placement<H extends StatisticHolder> extends Iterable<Placed<H>> {
	static PlayerOrder fromDeathOrder(IGamePhase game, List<PlayerKey> deathOrder) {
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

		return new PlayerOrder(game, order);
	}

	static <T extends Comparable<T>> Score<PlayerKey, T> fromPlayerScore(PlacementOrder order, IGamePhase game, StatisticKey<T> statistic) {
		return fromPlayerScore(order, game, statistic, false);
	}

	static <T extends Comparable<T>> Score<GameTeamKey, T> fromTeamScore(PlacementOrder order, IGamePhase game, StatisticKey<T> statistic, @Nullable T defaultValue) {
		TeamState teams = game.instanceState().getOrNull(TeamState.KEY);
		return fromScore(game, teams != null ? teams.getTeamKeys() : List.of(), statistic, order.asComparator(), defaultValue);
	}

	static <T extends Comparable<T>> Score<PlayerKey, T> fromPlayerScore(PlacementOrder order, IGamePhase game, StatisticKey<T> statistic, boolean onlyOnline) {
		List<PlayerKey> players = new ArrayList<>(game.statistics().getPlayers());
		if (onlyOnline) {
			players.removeIf(key -> !game.allPlayers().contains(key.id()));
		}
		return fromScore(game, players, statistic, order.asComparator(), null);
	}

	static <H extends StatisticHolder, T> Score<H, T> fromScore(IGamePhase game, Collection<H> scoreHolders, StatisticKey<T> scoreKey, Comparator<T> comparator, @Nullable T defaultValue) {
		GameStatistics statistics = game.statistics();

		List<H> sortedHolders = new ArrayList<>(scoreHolders);
		sortedHolders.sort(Comparator.comparing(
				holder -> holder.getOwnStatistics(statistics).get(scoreKey),
				Comparator.nullsLast(comparator)
		));

		List<Score.Entry<H, T>> entries = new ArrayList<>(sortedHolders.size());

		int placement = 0;
		T lastScore = null;

		for (H holder : sortedHolders) {
			StatisticsMap ownStatistics = holder.getOwnStatistics(statistics);
			T score = ownStatistics.getOr(scoreKey, defaultValue);
			if (score == null) {
				continue;
			}

			if (!Objects.equals(score, lastScore) || placement == 0) {
				placement++;
			}
			lastScore = score;

			entries.add(new Score.Entry<>(holder, placement, score));
		}

		return new Score<>(game, scoreKey, entries);
	}

	void placeInto(StatisticKey<Integer> placementKey);

	void sendTo(PlayerSet players, int length);

	void addToSidebar(List<Component> sidebar, int length);

	@Nullable
	H getWinner();

	final class PlayerOrder implements Placement<PlayerKey> {
		private final IGamePhase game;
		private final List<Placed<PlayerKey>> order;

		PlayerOrder(IGamePhase game, List<Placed<PlayerKey>> order) {
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
		@Nullable
		public PlayerKey getWinner() {
			return !order.isEmpty() ? order.getFirst().value() : null;
		}

		@Override
		public Iterator<Placed<PlayerKey>> iterator() {
			return order.iterator();
		}
	}

	final class Score<H extends StatisticHolder, T> implements Placement<H> {
		private final IGamePhase game;
		private final StatisticKey<T> scoreKey;
		private final List<Entry<H, T>> entries;

		Score(IGamePhase game, StatisticKey<T> scoreKey, List<Entry<H, T>> entries) {
			this.game = game;
			this.scoreKey = scoreKey;
			this.entries = entries;
		}

		@Override
		public void placeInto(StatisticKey<Integer> placementKey) {
			GameStatistics statistics = game.statistics();
			statistics.clear(placementKey);

			for (Entry<H, T> entry : entries) {
				entry.holder.getOwnStatistics(statistics).set(placementKey, entry.placement);
			}
		}

		@Override
		public void sendTo(PlayerSet players, int length) {
			int i = 0;
			Entry<H, T> entry;

			for (int place = 1; place <= length; place++) {
				String headPrefix = " " + place + ". ";
				String indentPrefix = StringUtils.repeat(' ', headPrefix.length());

				boolean head = true;
				while (i < entries.size() && (entry = entries.get(i)).placement == place) {
					String prefix = head ? headPrefix : indentPrefix;
					head = false;

					MutableComponent name = Component.literal(prefix).append(entry.holder.getName(game)).append(": ");
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
				Entry<H, T> entry = entries.get(i);
				Component name = Component.empty().append(entry.holder.getName(game)).append(": ").withStyle(ChatFormatting.AQUA);
				Component score = Component.literal(scoreKey.display(entry.score)).withStyle(ChatFormatting.GOLD);
				sidebar.add(Component.literal(" - ").append(name).append(score));
			}
		}

		@Override
		@Nullable
		public H getWinner() {
			if (entries.isEmpty()) {
				return null;
			}
			Entry<H, T> first = entries.getFirst();
			// The winning score is ambiguous
			if (entries.size() > 1 && first.score.equals(entries.get(1).score)) {
				return null;
			}
			return first.holder;
		}

		@Override
		public Iterator<Placed<H>> iterator() {
			return Iterators.transform(entries.iterator(), input -> new Placed<>(input.placement, input.holder));
		}

		record Entry<H, T>(H holder, int placement, T score) {
		}
	}
}
