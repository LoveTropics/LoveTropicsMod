package com.lovetropics.minigames.common.core.game.statistics;

import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.PlayerSet;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public interface PlayerPlacement {
	static Order fromDeathOrder(IGameInstance minigame, List<PlayerKey> deathOrder) {
		PlayerSet participants = minigame.getParticipants();
		List<Placed<PlayerKey>> order = new ArrayList<>(participants.size() + deathOrder.size());

		boolean anyFirst = false;
		for (ServerPlayerEntity player : participants) {
			order.add(Placed.at(1, PlayerKey.from(player)));
			anyFirst = true;
		}

		int placement = anyFirst ? 1 : 0;
		for (int i = deathOrder.size() - 1; i >= 0; i--) {
			order.add(Placed.at(++placement, deathOrder.get(i)));
		}

		return new Order(minigame, order);
	}

	static <T extends Comparable<T>> Score<T> fromMinScore(IGameInstance minigame, StatisticKey<T> score) {
		return fromScore(minigame, score, Comparator.naturalOrder());
	}

	static <T extends Comparable<T>> Score<T> fromMaxScore(IGameInstance minigame, StatisticKey<T> score) {
		return fromScore(minigame, score, Comparator.reverseOrder());
	}

	static <T> Score<T> fromScore(IGameInstance minigame, StatisticKey<T> scoreKey, Comparator<T> comparator) {
		GameStatistics statistics = minigame.getStatistics();

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

		return new Score<>(minigame, scoreKey, entries);
	}

	void placeInto(StatisticKey<Integer> placementKey);

	void sendTo(PlayerSet players, int length);

	void addToSidebar(List<String> sidebar, int length);

	final class Order implements PlayerPlacement {
		private final IGameInstance minigame;
		private final List<Placed<PlayerKey>> order;

		Order(IGameInstance minigame, List<Placed<PlayerKey>> order) {
			this.minigame = minigame;
			this.order = order;
		}

		@Override
		public void placeInto(StatisticKey<Integer> placementKey) {
			GameStatistics statistics = minigame.getStatistics();
			statistics.clear(placementKey);

			for (Placed<PlayerKey> placed : order) {
				statistics.forPlayer(placed.value).set(placementKey, placed.placement);
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
				while (i < order.size() && (entry = order.get(i)).placement == place) {
					String prefix = head ? headPrefix : indentPrefix;
					head = false;

					players.sendMessage(new StringTextComponent(prefix).mergeStyle(TextFormatting.AQUA)
							.appendSibling(new StringTextComponent(entry.value.getName()).mergeStyle(TextFormatting.GOLD))
					);

					i++;
				}
			}
		}

		@Override
		public void addToSidebar(List<String> sidebar, int length) {
			length = Math.min(order.size(), length);
			for (int i = 0; i < length; i++) {
				Placed<PlayerKey> entry = order.get(i);
				sidebar.add(" - " + TextFormatting.AQUA + entry.value.getName());
			}
		}
	}

	final class Score<T> implements PlayerPlacement {
		private final IGameInstance minigame;
		private final StatisticKey<T> scoreKey;
		private final List<Entry<T>> entries;

		Score(IGameInstance minigame, StatisticKey<T> scoreKey, List<Entry<T>> entries) {
			this.minigame = minigame;
			this.scoreKey = scoreKey;
			this.entries = entries;
		}

		@Override
		public void placeInto(StatisticKey<Integer> placementKey) {
			GameStatistics statistics = minigame.getStatistics();
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

					IFormattableTextComponent name = new StringTextComponent(prefix + entry.player.getName() + ": ");
					IFormattableTextComponent score = new StringTextComponent(scoreKey.display(entry.score));

					players.sendMessage(name.mergeStyle(TextFormatting.AQUA).appendSibling(score.mergeStyle(TextFormatting.GOLD)));

					i++;
				}
			}
		}

		@Override
		public void addToSidebar(List<String> sidebar, int length) {
			length = Math.min(entries.size(), length);
			for (int i = 0; i < length; i++) {
				Entry<T> entry = entries.get(i);
				sidebar.add(" - " + TextFormatting.AQUA + entry.player.getName() + ": " + TextFormatting.GOLD + scoreKey.display(entry.score));
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
