package com.lovetropics.minigames.common.content.river_race.block;

import com.lovetropics.minigames.common.content.river_race.behaviour.TriviaBehaviour;
import com.lovetropics.minigames.common.core.network.trivia.ShowTriviaMessage;
import com.lovetropics.minigames.common.content.river_race.event.RiverRaceEvents;
import com.lovetropics.minigames.common.core.game.IGameManager;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

import java.util.Optional;

public class TriviaBlockEntity extends BlockEntity {

    public record TriviaBlockState(boolean isAnswered, Optional<String> correctAnswer, boolean lockedOut, long unlocksAt){
        public static final StreamCodec<ByteBuf, TriviaBlockState> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL, TriviaBlockState::isAnswered,
                ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), TriviaBlockState::correctAnswer,
                ByteBufCodecs.BOOL, TriviaBlockState::lockedOut,
                ByteBufCodecs.VAR_LONG, TriviaBlockState::unlocksAt,
                TriviaBlockState::new
        );
    }
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String TAG_QUESTION = "question";
    private static final String TAG_UNLOCKS_AT = "unlocksAt";
    private static final String TAG_ANSWERED = "answered";
    private TriviaBehaviour.TriviaQuestion question;
    private long unlocksAt;
    private boolean answered;
    private TriviaBlock.TriviaBlockType triviaBlockType;
    public TriviaBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        if(blockState.getBlock() instanceof TriviaBlock triviaBlock){
            this.triviaBlockType = triviaBlock.getType();
        }
    }

    public TriviaBlockEntity setTriviaType(TriviaBlock.TriviaBlockType blockType){
        this.triviaBlockType = blockType;
        return this;
    }

    public TriviaBlock.TriviaBlockType getTriviaBlockType() {
        return triviaBlockType;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if(question != null) {
            tag.put(TAG_QUESTION, TriviaBehaviour.TriviaQuestion.CODEC.encodeStart(NbtOps.INSTANCE, question).getOrThrow());
        }
        if(unlocksAt > 0){
            tag.putLong(TAG_UNLOCKS_AT, unlocksAt);
        }
        tag.putBoolean(TAG_ANSWERED, answered);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if(tag.contains(TAG_QUESTION)) {
            TriviaBehaviour.TriviaQuestion.CODEC.parse(NbtOps.INSTANCE, tag.get(TAG_QUESTION))
                    .resultOrPartial(LOGGER::error)
                    .ifPresent(q -> question = q);
        }
        if(tag.contains(TAG_UNLOCKS_AT)) {
            unlocksAt = tag.getLong(TAG_UNLOCKS_AT);
        }
        if (tag.contains(TAG_ANSWERED)) {
            answered = tag.getBoolean(TAG_ANSWERED);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        if(unlocksAt > 0) {
            tag.putLong(TAG_UNLOCKS_AT, unlocksAt);
        }
        tag.putBoolean(TAG_ANSWERED, answered);
        return tag;
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
        if (pkt.getTag() != null) {
            handleUpdateTag(pkt.getTag(), registries);
        }
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        if (tag.contains(TAG_UNLOCKS_AT)) {
            unlocksAt = tag.getLong(TAG_UNLOCKS_AT);
        }
        if (tag.contains(TAG_ANSWERED)) {
            answered = tag.getBoolean(TAG_ANSWERED);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void markUpdated() {
        setChanged();
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    public boolean hasQuestion(){
        return question != null;
    }

    public void setQuestion(TriviaBehaviour.TriviaQuestion question) {
        this.question = question;
        markUpdated();
    }

    public TriviaBehaviour.TriviaQuestion getQuestion() {
        return question;
    }

    public long lockout(int lockoutSeconds){
        unlocksAt = System.currentTimeMillis() + (lockoutSeconds * 1000L);
        markUpdated();
        return unlocksAt;
    }
    public void unlock() {
        unlocksAt = 0;
        markUpdated();
    }

    public void markAsCorrect(){
        answered = true;
        markUpdated();
    }

    public TriviaBlockState getState(){
        Optional<String> correctAnswer = Optional.empty();
        if(answered){
            correctAnswer = Optional.of(question.answers().stream().filter(TriviaBehaviour.TriviaQuestion.TriviaQuestionAnswer::correct).findFirst().get().text());
        }
        return new TriviaBlockState(answered, correctAnswer, unlocksAt > 0, unlocksAt);
    }

}
