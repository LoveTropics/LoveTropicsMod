package com.lovetropics.minigames.common.core.network.trivia;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.client.game.trivia.ClientTriviaHandler;
import com.lovetropics.minigames.common.content.river_race.behaviour.TriviaBehaviour;
import com.lovetropics.minigames.common.content.river_race.block.TriviaBlockEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SelectTriviaAnswerMessage(BlockPos triviaBlock, String selectedAnswer) implements CustomPacketPayload {

    public static final Type<SelectTriviaAnswerMessage> TYPE = new Type<>(LoveTropics.location("select_trivia_answer"));
    public static final StreamCodec<ByteBuf, SelectTriviaAnswerMessage> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SelectTriviaAnswerMessage::triviaBlock,
            ByteBufCodecs.STRING_UTF8, SelectTriviaAnswerMessage::selectedAnswer,
            SelectTriviaAnswerMessage::new
    );

    public static void handle(final SelectTriviaAnswerMessage message, final IPayloadContext context) {
        if(context.player().level().getBlockEntity(message.triviaBlock()) instanceof TriviaBlockEntity triviaBlockEntity){
            triviaBlockEntity.handleAnswerSelection(context.player(), message.selectedAnswer());
        }
    }
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
