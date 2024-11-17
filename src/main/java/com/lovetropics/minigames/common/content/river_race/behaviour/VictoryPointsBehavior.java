package com.lovetropics.minigames.common.content.river_race.behaviour;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.SoundRegistry;
import com.lovetropics.minigames.common.content.river_race.RiverRaceTexts;
import com.lovetropics.minigames.common.content.river_race.block.TriviaType;
import com.lovetropics.minigames.common.content.river_race.event.RiverRaceEvents;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.GameWinner;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLogicEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.SubGameEvents;
import com.lovetropics.minigames.common.core.game.player.PlayerSet;
import com.lovetropics.minigames.common.core.game.state.statistics.GameStatistics;
import com.lovetropics.minigames.common.core.game.state.statistics.Placement;
import com.lovetropics.minigames.common.core.game.state.statistics.PlacementOrder;
import com.lovetropics.minigames.common.core.game.state.statistics.PlayerKey;
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticKey;
import com.lovetropics.minigames.common.core.game.state.team.GameTeam;
import com.lovetropics.minigames.common.core.game.state.team.GameTeamKey;
import com.lovetropics.minigames.common.core.game.state.team.TeamState;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ExtraCodecs;

import javax.annotation.Nullable;
import java.util.List;

public class VictoryPointsBehavior implements IGameBehavior {

    public static final MapCodec<VictoryPointsBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Codec.INT.optionalFieldOf("trivia_chest_points", 1).forGetter(c -> c.triviaChestPoints),
        Codec.INT.optionalFieldOf("trivia_gate_points", 2).forGetter(c -> c.triviaGatePoints),
        Codec.INT.optionalFieldOf("trivia_challenge_points", 5).forGetter(c -> c.triviaChallengePoints),
        Codec.INT.optionalFieldOf("collectable_collected_points", 0).forGetter(c -> c.collectableCollectedPoints),
        Codec.INT.optionalFieldOf("collectable_placed_points", 1).forGetter(c -> c.collectablePlacedPoints),
        ExtraCodecs.nonEmptyList(MoreCodecs.listOrUnit(Codec.INT)).optionalFieldOf("points_per_game_won", List.of(3, 2, 1)).forGetter(c -> c.pointsPerGameWon)
    ).apply(i, VictoryPointsBehavior::new));

    private IGamePhase game;

    private final int triviaChestPoints;
    private final int triviaGatePoints;
    private final int triviaChallengePoints;
    private final int collectableCollectedPoints;
    private final int collectablePlacedPoints;
    private final List<Integer> pointsPerGameWon;

    @Nullable
    private MicrogameSegmentState microgameSegment;

    public VictoryPointsBehavior(int triviaChestPoints, int triviaGatePoints, int triviaChallengePoints, int collectableCollectedPoints, int collectablePlacedPoints, List<Integer> pointsPerGameWon) {
        this.triviaChestPoints = triviaChestPoints;
        this.triviaGatePoints = triviaGatePoints;
        this.triviaChallengePoints = triviaChallengePoints;
        this.collectableCollectedPoints = collectableCollectedPoints;
        this.collectablePlacedPoints = collectablePlacedPoints;
        this.pointsPerGameWon = pointsPerGameWon;
    }

    @Override
    public void register(IGamePhase game, EventRegistrar events) throws GameException {
        this.game = game;
        TeamState teams = game.instanceState().getOrThrow(TeamState.KEY);

        events.listen(RiverRaceEvents.QUESTION_COMPLETED, this::onQuestionAnswered);
        events.listen(RiverRaceEvents.COLLECTABLE_PLACED, this::onCollectablePlaced);
        events.listen(RiverRaceEvents.VICTORY_POINTS_CHANGED, (team, value, lastValue) -> {
            PlayerSet playersForTeam = teams.getPlayersForTeam(team);
            playersForTeam.sendMessage(RiverRaceTexts.VICTORY_POINT_CHANGE.apply(value - lastValue), true);
            playersForTeam.playSound(SoundRegistry.COINS.value(), SoundSource.NEUTRAL, 0.4f, 1);
        });

        Object2IntMap<GameTeamKey> lastTeamPoints = new Object2IntArrayMap<>();
        events.listen(GamePhaseEvents.TICK, () -> {
            for (GameTeamKey teamKey : teams.getTeamKeys()) {
                int newPoints = game.statistics().forTeam(teamKey).getInt(StatisticKey.VICTORY_POINTS);
                int oldPoints = lastTeamPoints.put(teamKey, newPoints);
                if (newPoints != oldPoints) {
                    game.invoker(RiverRaceEvents.VICTORY_POINTS_CHANGED).onVictoryPointsChanged(teamKey, newPoints, oldPoints);
                }
            }
        });

        events.listen(SubGameEvents.CREATE, (subGame, subEvents) -> {
            if (microgameSegment == null) {
                microgameSegment = new MicrogameSegmentState();
            }
            MicrogameSegmentState segment = microgameSegment;
            subEvents.listen(GameLogicEvents.GAME_OVER, winner -> onMicrogameWinTriggered(winner, segment));
        });
        events.listen(SubGameEvents.RETURN_TO_TOP, () -> {
			if (microgameSegment != null) {
                onMicrogamesCompleted(microgameSegment);
                microgameSegment = null;
            }
        });
    }

    private void onMicrogameWinTriggered(GameWinner winner, MicrogameSegmentState segmentState) {
        GameTeamKey winningTeam = switch (winner) {
            case GameWinner.Player(ServerPlayer player) -> getTeamFor(PlayerKey.from(player));
            case GameWinner.Team(GameTeam team) -> team.key();
            case GameWinner.OfflinePlayer(PlayerKey playerKey, Component ignored) -> getTeamFor(playerKey);
            case GameWinner.Nobody ignored -> null;
		};
		if (winningTeam != null) {
            int winIndex = segmentState.winCountByTeam.addTo(winningTeam, 1);
            int points = pointsPerGameWon.get(Math.min(winIndex, pointsPerGameWon.size() - 1));
            segmentState.pointsByTeam.addTo(winningTeam, points);
		}
    }

    private void onMicrogamesCompleted(MicrogameSegmentState segmentState) {
        GameStatistics segmentStatistics = new GameStatistics();

        for (Object2IntMap.Entry<GameTeamKey> entry : segmentState.pointsByTeam.object2IntEntrySet()) {
            addPoints(entry.getKey(), entry.getIntValue());
            segmentStatistics.forTeam(entry.getKey()).set(StatisticKey.VICTORY_POINTS, entry.getIntValue());
        }

        game.allPlayers().sendMessage(RiverRaceTexts.MICROGAME_RESULTS);
		Placement.fromScore(game, segmentStatistics, segmentStatistics.getTeams(), StatisticKey.VICTORY_POINTS, PlacementOrder.MAX.asComparator())
                .sendTo(game.allPlayers(), 5);
    }

    @Nullable
    private GameTeamKey getTeamFor(PlayerKey player) {
        TeamState teams = game.instanceState().getOrNull(TeamState.KEY);
        return teams != null ? teams.getTeamForPlayer(player) : null;
    }

    private void addPoints(final PlayerKey playerKey, final int points) {
        TeamState teams = game.instanceState().getOrNull(TeamState.KEY);
        GameTeamKey team = teams != null ? teams.getTeamForPlayer(playerKey) : null;
		if (team != null) {
            game.statistics().forTeam(team).incrementInt(StatisticKey.VICTORY_POINTS, points);
        }
    }

    private void addPoints(final GameTeamKey team, final int points) {
        game.statistics().forTeam(team).incrementInt(StatisticKey.VICTORY_POINTS, points);
    }

    private void onQuestionAnswered(ServerPlayer player, TriviaType triviaType, BlockPos triviaPos) {
		int points = switch (triviaType) {
            case COLLECTABLE -> collectableCollectedPoints;
            case VICTORY -> triviaChallengePoints;
            case REWARD -> triviaChestPoints;
            case GATE -> triviaGatePoints;
        };
        addPoints(PlayerKey.from(player), points);
    }

    private void onCollectablePlaced(ServerPlayer player, GameTeam team, BlockPos pos) {
        addPoints(PlayerKey.from(player), collectablePlacedPoints);
    }

	private static final class MicrogameSegmentState {
		private final Object2IntOpenHashMap<GameTeamKey> winCountByTeam = new Object2IntOpenHashMap<>();
		private final Object2IntOpenHashMap<GameTeamKey> pointsByTeam = new Object2IntOpenHashMap<>();
	}
}
