package com.lovetropics.minigames.common.content.river_race.block;

import com.lovetropics.minigames.common.content.river_race.behaviour.TriviaBehaviour;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

public class TriviaBlockEntity extends BlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String TAG_QUESTION = "question";
    private static final String TAG_UNLOCKS_AT = "unlocksAt";
    private TriviaBehaviour.TriviaQuestion question;
    private long unlocksAt;
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
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        if(unlocksAt > 0) {
            tag.putLong(TAG_UNLOCKS_AT, unlocksAt);
        }
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

    public void handleAnswerSelection(Player player, String selectedAnswer){
        if(getQuestion() != null){
            TriviaBehaviour.TriviaQuestion.TriviaQuestionAnswer answer = getQuestion().getAnswer(selectedAnswer);
            if(answer != null){
                if(answer.correct()){
                    player.sendSystemMessage(Component.
                            literal("Correct! Do something here!")
                            .withStyle(ChatFormatting.GREEN));
                } else {
                    //TODO: Make this a translation key
                    player.sendSystemMessage(Component.
                            literal("Incorrect! This question is now locked out for x seconds!")
                            .withStyle(ChatFormatting.RED));
                    unlocksAt = System.currentTimeMillis();
                    markUpdated();
                }
            }
        }
    }

}
