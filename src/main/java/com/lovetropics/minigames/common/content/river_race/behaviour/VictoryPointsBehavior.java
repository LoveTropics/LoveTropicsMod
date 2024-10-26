package com.lovetropics.minigames.common.content.river_race.behaviour;

import com.lovetropics.minigames.common.content.river_race.block.TriviaBlock;
import com.lovetropics.minigames.common.content.river_race.event.RiverRaceEvents;
import com.lovetropics.minigames.common.content.river_race.state.RiverRaceState;
import com.lovetropics.minigames.common.content.river_race.state.VictoryPointsGameState;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLogicEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.state.team.GameTeam;
import com.lovetropics.minigames.common.core.game.state.team.GameTeamKey;
import com.lovetropics.minigames.common.core.game.state.team.TeamState;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Objects;

public class VictoryPointsBehavior implements IGameBehavior {

    public static final MapCodec<VictoryPointsBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Codec.INT.optionalFieldOf("points_per_question", 1).forGetter(c -> c.pointsPerQuestion),
        Codec.INT.optionalFieldOf("points_per_block_collected", 1).forGetter(c -> c.pointsPerBlockCollected),
        Codec.INT.optionalFieldOf("points_per_game_won", 1).forGetter(c -> c.pointsPerGameWon)
    ).apply(i, VictoryPointsBehavior::new));

    private IGamePhase game;

    private final int pointsPerQuestion;
    private final int pointsPerBlockCollected;
    private final int pointsPerGameWon;

    public VictoryPointsBehavior(final int pointsPerQuestion, final int pointsPerBlockCollected, final int pointsPerGameWon) {
        this.pointsPerQuestion = pointsPerQuestion;
        this.pointsPerBlockCollected = pointsPerBlockCollected;
        this.pointsPerGameWon = pointsPerGameWon;
    }

    @Override
    public void register(IGamePhase game, EventRegistrar events) throws GameException {
        this.game = game;

        // Victory points from trivia
        events.listen(RiverRaceEvents.ANSWER_QUESTION, this::onQuestionAnswered);
        // Victory points from collectible blocks
        events.listen(GamePlayerEvents.BREAK_BLOCK, this::onBlockBroken);
        // Victory points from winning microgame
        events.listen(GameLogicEvents.WIN_TRIGGERED, this::onWinTriggered);
    }

    private void onWinTriggered(Component component) {
        for (final ServerPlayer player : game.participants()) {
            if (Objects.equals(player.getDisplayName(), component)) {
                tryAddPoints(player, pointsPerGameWon);
                player.displayClientMessage(Component.literal("YOU WIN!!!! Victory points for team: " + getPoints(player)), false);
                return;
            }
        }

        var teams = game.instanceState().getOrNull(TeamState.KEY);
        if (teams == null) return;
        for (final GameTeam team : teams) {
            if (Objects.equals(team.config().name(), component)) {
                tryAddPoints(team.key(), pointsPerGameWon);
            }
        }
    }

    private void tryAddPoints(final ServerPlayer player, final int points) {
        final VictoryPointsGameState pointState = state();
        if (pointState != null) {
            pointState.addPointsToTeam(player, points);
        }
    }

    private void tryAddPoints(final GameTeamKey team, final int points) {
        final VictoryPointsGameState pointState = state();
        if (pointState != null) {
            pointState.addPointsToTeam(team, points);
        }
    }

    private int getPoints(final ServerPlayer player) {
        final VictoryPointsGameState gameState = state();
        if (gameState != null) {
            return gameState.getVictoryPoints(player);
        }
        return -1;
    }

    @Nullable
    private VictoryPointsGameState state() {
        // TODO how to make this more generic to not be specific to river race?
        return game.state().getOrNull(RiverRaceState.KEY);
    }

    private InteractionResult onBlockBroken(ServerPlayer serverPlayer, BlockPos blockPos, BlockState blockState, InteractionHand interactionHand) {
        return InteractionResult.PASS;
    }

    private void onQuestionAnswered(ServerPlayer player, TriviaBlock.TriviaType triviaType, boolean correct) {
        if (correct) {
            if(triviaType == TriviaBlock.TriviaType.COLLECTABLE) {
                tryAddPoints(player, pointsPerQuestion);
            }
            player.displayClientMessage(Component.literal("CORRECT! Victory points for team: " + getPoints(player)), false);
        } else {
            player.displayClientMessage(Component.literal("WRONG >:( Victory points for team: " + getPoints(player)), false);
        }
    }
}
