package com.lovetropics.minigames.common.core.network.trivia;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.content.river_race.TriviaEvents;
import com.lovetropics.minigames.common.content.river_race.block.HasTrivia;
import com.lovetropics.minigames.common.content.river_race.block.TriviaBlockEntity;
import com.lovetropics.minigames.common.core.game.IGameManager;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SelectTriviaAnswerMessage(BlockPos triviaBlock, String selectedAnswer) implements CustomPacketPayload {
    public static final Type<SelectTriviaAnswerMessage> TYPE = new Type<>(LoveTropics.location("select_trivia_answer"));
    public static final StreamCodec<ByteBuf, SelectTriviaAnswerMessage> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SelectTriviaAnswerMessage::triviaBlock,
            ByteBufCodecs.STRING_UTF8, SelectTriviaAnswerMessage::selectedAnswer,
            SelectTriviaAnswerMessage::new
    );

    public static void handle(final SelectTriviaAnswerMessage message, final IPayloadContext context) {
        if (context.player().level().getBlockEntity(message.triviaBlock()) instanceof final HasTrivia triviaBlockEntity){
            IGamePhase game = IGameManager.get().getGamePhaseFor(context.player());
            if (game != null) {
                ServerPlayer player = (ServerPlayer) context.player();
                boolean isCorrect = game.invoker(TriviaEvents.ANSWER_TRIVIA_BLOCK_QUESTION)
                        .onAnswerQuestion(player, player.serverLevel(), message.triviaBlock(),
                                triviaBlockEntity,
                                triviaBlockEntity.getQuestion(), message.selectedAnswer());

            }
//            triviaBlockEntity.handleAnswerSelection(context.player(), message.selectedAnswer());
        }
    }
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
