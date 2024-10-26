package com.lovetropics.minigames.common.core.network.trivia;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.client.game.trivia.ClientTriviaHandler;
import com.lovetropics.minigames.common.content.river_race.behaviour.TriviaBehaviour;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ShowTriviaMessage(BlockPos triviaBlock, TriviaBehaviour.TriviaQuestion question) implements CustomPacketPayload {

    public static final Type<ShowTriviaMessage> TYPE = new Type<>(LoveTropics.location("show_trivia"));
    public static final StreamCodec<ByteBuf, ShowTriviaMessage> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ShowTriviaMessage::triviaBlock,
            TriviaBehaviour.TriviaQuestion.STREAM_CODEC, ShowTriviaMessage::question,
            ShowTriviaMessage::new
    );

    public static void handle(final ShowTriviaMessage message, final IPayloadContext context) {
        ClientTriviaHandler.showScreen(message);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
