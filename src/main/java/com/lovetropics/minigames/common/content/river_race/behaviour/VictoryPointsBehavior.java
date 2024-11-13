package com.lovetropics.minigames.common.content.river_race.behaviour;

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
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.player.PlayerSet;
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
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.state.BlockState;

public class VictoryPointsBehavior implements IGameBehavior {

    public static final MapCodec<VictoryPointsBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Codec.INT.optionalFieldOf("trivia_chest_points", 1).forGetter(c -> c.triviaChestPoints),
        Codec.INT.optionalFieldOf("trivia_gate_points", 2).forGetter(c -> c.triviaGatePoints),
        Codec.INT.optionalFieldOf("trivia_challenge_points", 5).forGetter(c -> c.triviaChallengePoints),
        Codec.INT.optionalFieldOf("collectible_collected_points", 1).forGetter(c -> c.collectibleCollectedPoints),
        Codec.INT.optionalFieldOf("points_per_game_won", 3).forGetter(c -> c.pointsPerGameWon)
    ).apply(i, VictoryPointsBehavior::new));

    private IGamePhase game;

    private final int triviaChestPoints;
    private final int triviaGatePoints;
    private final int triviaChallengePoints;
    private final int collectibleCollectedPoints;
    private final int pointsPerGameWon;

    public VictoryPointsBehavior(int triviaChestPoints, int triviaGatePoints, int triviaChallengePoints, int collectibleCollectedPoints, int pointsPerGameWon) {
        this.triviaChestPoints = triviaChestPoints;
        this.triviaGatePoints = triviaGatePoints;
        this.triviaChallengePoints = triviaChallengePoints;
        this.collectibleCollectedPoints = collectibleCollectedPoints;
        this.pointsPerGameWon = pointsPerGameWon;
    }

    @Override
    public void register(IGamePhase game, EventRegistrar events) throws GameException {
        this.game = game;
        TeamState teams = game.instanceState().getOrThrow(TeamState.KEY);

        // Victory points from trivia
        events.listen(RiverRaceEvents.QUESTION_COMPLETED, this::onQuestionAnswered);
        // Victory points from collectible blocks
        events.listen(GamePlayerEvents.BREAK_BLOCK, this::onBlockBroken);
        // Victory points from winning microgame
        events.listen(GameLogicEvents.WIN_TRIGGERED, this::onWinTriggered);
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
    }

    private void onWinTriggered(GameWinner winner) {
		switch (winner) {
            case GameWinner.Player(ServerPlayer player) -> tryAddPoints(PlayerKey.from(player), pointsPerGameWon);
            case GameWinner.Team(GameTeam team) -> tryAddPoints(team.key(), pointsPerGameWon);
            case GameWinner.OfflinePlayer(PlayerKey playerKey, Component ignored) -> tryAddPoints(playerKey, pointsPerGameWon);
            case GameWinner.Nobody ignored -> {
            }
		}
    }

    private void tryAddPoints(final PlayerKey playerKey, final int points) {
        TeamState teams = game.instanceState().getOrNull(TeamState.KEY);
        GameTeamKey team = teams != null ? teams.getTeamForPlayer(playerKey) : null;
		if (team != null) {
            game.statistics().forTeam(team).incrementInt(StatisticKey.VICTORY_POINTS, points);
        }
    }

    private void tryAddPoints(final GameTeamKey team, final int points) {
        game.statistics().forTeam(team).incrementInt(StatisticKey.VICTORY_POINTS, points);
    }

    private InteractionResult onBlockBroken(ServerPlayer serverPlayer, BlockPos blockPos, BlockState blockState, InteractionHand interactionHand) {
        return InteractionResult.PASS;
    }

    private void onQuestionAnswered(ServerPlayer player, TriviaType triviaType, BlockPos triviaPos) {
        PlayerKey playerKey = PlayerKey.from(player);
        switch (triviaType) {
            case COLLECTABLE -> {
                tryAddPoints(playerKey, collectibleCollectedPoints);
                player.displayClientMessage(RiverRaceTexts.COLLECTABLE_GIVEN, false);
            }
            case VICTORY -> {
                tryAddPoints(playerKey, triviaChallengePoints);
                player.displayClientMessage(RiverRaceTexts.VICTORY_POINT_GIVEN, false);
            }
            case REWARD -> {
                tryAddPoints(playerKey, triviaChestPoints);
                player.displayClientMessage(RiverRaceTexts.LOOT_GIVEN, false);
            }
            case GATE -> tryAddPoints(playerKey, triviaGatePoints);
        }
    }
}
