package com.lovetropics.minigames.common.content.biodiversity_blitz.behavior;

import com.lovetropics.minigames.common.content.biodiversity_blitz.BiodiversityBlitz;
import com.lovetropics.minigames.common.content.biodiversity_blitz.BiodiversityBlitzTexts;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event.BbEvents;
import com.lovetropics.minigames.common.content.biodiversity_blitz.client_state.ClientBbScoreboardState;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.CurrencyManager;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.PlotsState;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.client_state.GameClientState;
import com.lovetropics.minigames.common.core.game.state.team.GameTeam;
import com.lovetropics.minigames.common.core.game.state.team.GameTeamKey;
import com.lovetropics.minigames.common.core.game.state.team.TeamState;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Supplier;

public record BbInGameScoreboardBehavior(Vec3 start, Vec3 end, boolean side, GameTeamKey leftTeam, GameTeamKey rightTeam) implements IGameBehavior {
	public static final MapCodec<BbInGameScoreboardBehavior> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Vec3.CODEC.fieldOf("start").forGetter(BbInGameScoreboardBehavior::start),
			Vec3.CODEC.fieldOf("end").forGetter(BbInGameScoreboardBehavior::end),
			Codec.BOOL.fieldOf("side").forGetter(BbInGameScoreboardBehavior::side),
			GameTeamKey.CODEC.fieldOf("left_team").forGetter(BbInGameScoreboardBehavior::leftTeam),
			GameTeamKey.CODEC.fieldOf("right_team").forGetter(BbInGameScoreboardBehavior::rightTeam)
	).apply(instance, BbInGameScoreboardBehavior::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		PlotsState plots = game.getState().getOrThrow(PlotsState.KEY);
		CurrencyManager currency = game.getState().getOrThrow(CurrencyManager.KEY);
		TeamState teams = game.getInstanceState().getOrThrow(TeamState.KEY);

		GameTeam leftTeam = teams.getTeamByKey(this.leftTeam);
		GameTeam rightTeam = teams.getTeamByKey(this.rightTeam);
		if (leftTeam == null || rightTeam == null) {
			throw new GameException(Component.literal("Missing teams for scoreboard"));
		}

		Tracker tracker = new Tracker(() -> createState(currency, plots, leftTeam, rightTeam));
		events.listen(GamePlayerEvents.ADD, tracker::add);
		events.listen(GamePlayerEvents.REMOVE, tracker::remove);

		events.listen(BbEvents.CURRENCY_INCREMENT_CHANGED, (team, value, lastValue) -> tracker.notifyChanged(game));
		events.listen(BbEvents.CURRENCY_ACCUMULATE, (team, value, lastValue) -> tracker.notifyChanged(game));
	}

	private ClientBbScoreboardState createState(CurrencyManager currency, PlotsState plots, GameTeam leftTeam, GameTeam rightTeam) {
		return new ClientBbScoreboardState(start, end, side, BiodiversityBlitzTexts.SCOREBOARD_TITLE, List.of(
				CommonComponents.EMPTY, CommonComponents.EMPTY,
				leftTeam.config().styledName(), rightTeam.config().styledName(),
				formatPoints(currency, leftTeam.key()), formatPoints(currency, rightTeam.key()),
				formatIncrement(plots, leftTeam.key()), formatIncrement(plots, rightTeam.key())
		));
	}

	private static Component formatPoints(CurrencyManager currency, GameTeamKey team) {
		Component points = Component.literal(String.valueOf(currency.getPoints(team))).withStyle(ChatFormatting.AQUA);
		return BiodiversityBlitzTexts.SCOREBOARD_POINTS.apply(points).withStyle(ChatFormatting.WHITE);
	}

	private static Component formatIncrement(PlotsState plots, GameTeamKey team) {
		Plot plot = plots.getPlotFor(team);
		Component points = Component.literal(String.valueOf(plot != null ? plot.nextCurrencyIncrement : 0)).withStyle(ChatFormatting.AQUA);
		return BiodiversityBlitzTexts.SCOREBOARD_POINTS_INCREMENT.apply(points).withStyle(ChatFormatting.WHITE);
	}

	private static class Tracker {
		private final Supplier<ClientBbScoreboardState> supplier;
		private ClientBbScoreboardState lastState;

		private Tracker(Supplier<ClientBbScoreboardState> supplier) {
			this.supplier = supplier;
			lastState = supplier.get();
		}

		public void notifyChanged(IGamePhase game) {
			ClientBbScoreboardState newState = supplier.get();
			if (!newState.equals(lastState)) {
				GameClientState.sendToPlayers(newState, game.getAllPlayers());
				lastState = newState;
			}
		}

		public void add(ServerPlayer player) {
			GameClientState.sendToPlayer(lastState, player);
		}

		public void remove(ServerPlayer player) {
			GameClientState.removeFromPlayer(BiodiversityBlitz.SCOREBOARD.get(), player);
		}
	}
}
