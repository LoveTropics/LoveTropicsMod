package com.lovetropics.minigames.common.content.river_race.behaviour;

import com.lovetropics.minigames.common.content.river_race.block.TriviaBlock;
import com.lovetropics.minigames.common.content.river_race.event.RiverRaceEvents;
import com.lovetropics.minigames.common.content.river_race.state.VictoryPointsState;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLogicEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.state.GameStateMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.state.BlockState;

public class VictoryPointsBehavior implements IGameBehavior {

    public static final MapCodec<VictoryPointsBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Codec.INT.optionalFieldOf("points_per_question", 1).forGetter(c -> c.pointsPerQuestion),
        Codec.INT.optionalFieldOf("points_per_block_collected", 1).forGetter(c -> c.pointsPerBlockCollected),
        Codec.INT.optionalFieldOf("points_per_game_won", 1).forGetter(c -> c.pointsPerGameWon)
    ).apply(i, VictoryPointsBehavior::new));

    private IGamePhase game;

    private VictoryPointsState points;

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
        // TODO is this the right event to use here?
        events.listen(GameLogicEvents.GAME_OVER, this::onGameOver);
    }

    @Override
    public void registerState(IGamePhase game, GameStateMap phaseState, GameStateMap instanceState) {
        points = phaseState.register(VictoryPointsState.KEY, new VictoryPointsState(game));
    }

    private void onGameOver() {

    }

    private InteractionResult onBlockBroken(ServerPlayer serverPlayer, BlockPos blockPos, BlockState blockState, InteractionHand interactionHand) {
        return InteractionResult.PASS;
    }

    private void onQuestionAnswered(ServerPlayer player, TriviaBlock.TriviaType triviaType, boolean correct) {
        if (correct) {
            if(triviaType == TriviaBlock.TriviaType.COLLECTABLE) {
                points.addPointsToTeam(player, pointsPerQuestion);
            }
            player.displayClientMessage(Component.literal("CORRECT! Victory points for team: " + points.getPoints(player)), false);
        } else {
            player.displayClientMessage(Component.literal("WRONG >:( Victory points for team: " + points.getPoints(player)), false);
        }
    }
}
