package com.lovetropics.minigames.common.core.game.behavior.instances.action;

import com.lovetropics.minigames.common.core.game.GameWinner;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameActionEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLogicEvents;
import com.lovetropics.minigames.common.core.game.state.team.GameTeam;
import com.lovetropics.minigames.common.core.game.state.team.GameTeamKey;
import com.lovetropics.minigames.common.core.game.state.team.TeamState;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;

import javax.annotation.Nullable;
import java.util.Optional;

public record EndGameAction(
		Optional<GameTeamKey> winningTeam,
		Optional<Source> winBySource
) implements IGameBehavior {
	public static final MapCodec<EndGameAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			GameTeamKey.CODEC.optionalFieldOf("winning_team").forGetter(EndGameAction::winningTeam),
			Source.CODEC.optionalFieldOf("win_by_source").forGetter(EndGameAction::winBySource)
	).apply(i, EndGameAction::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		if (winBySource.isPresent()) {
			Source source = winBySource.get();
			events.listen(GameActionEvents.APPLY_TO_PLAYER, (context, target) -> {
				game.invoker(GameLogicEvents.GAME_OVER).onGameOver(getWinnerBySource(game, target, source));
				return true;
			});
		} else {
			events.listen(GameActionEvents.APPLY, context -> {
				GameWinner winner = getFixedWinner(game);
				if (winner != null) {
					game.invoker(GameLogicEvents.GAME_OVER).onGameOver(winner);
				} else {
					if (!game.invoker(GameLogicEvents.REQUEST_GAME_OVER).requestGameOver()) {
						game.invoker(GameLogicEvents.GAME_OVER).onGameOver(new GameWinner.Nobody());
					}
				}
				return true;
			});
		}
	}

	@Nullable
	private GameWinner getFixedWinner(IGamePhase game) {
		return winningTeam.map(key -> {
			TeamState teams = game.instanceState().getOrThrow(TeamState.KEY);
			return teams.getTeamByKey(key);
		}).map(GameWinner.Team::new).orElse(null);
	}

	private GameWinner getWinnerBySource(IGamePhase game, ServerPlayer target, Source source) {
		return switch (source) {
			case PLAYER -> new GameWinner.Player(target);
			case TEAM -> {
				TeamState teams = game.instanceState().getOrThrow(TeamState.KEY);
				GameTeamKey teamKey = teams.getTeamForPlayer(target);
				GameTeam team = teamKey != null ? teams.getTeamByKey(teamKey) : null;
				yield team != null ? new GameWinner.Team(team) : new GameWinner.Nobody();
			}
		};
	}

	public enum Source implements StringRepresentable {
		PLAYER("player"),
		TEAM("team"),
		;

		public static final Codec<Source> CODEC = StringRepresentable.fromEnum(Source::values);

		private final String id;

		Source(String id) {
			this.id = id;
		}

		@Override
		public String getSerializedName() {
			return id;
		}
	}
}
