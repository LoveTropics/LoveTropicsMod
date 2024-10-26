package com.lovetropics.minigames.common.content.river_race.block;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.content.river_race.RiverRace;
import com.lovetropics.minigames.common.content.river_race.behaviour.TriviaBehaviour;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

import java.util.Optional;

public class TriviaChestBlockEntity extends ChestBlockEntity implements HasTrivia {
    private static final Logger LOGGER = LogUtils.getLogger();
    private TriviaBehaviour.TriviaQuestion question;
    private long unlocksAt;
    private boolean answered;

    public TriviaChestBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public TriviaChestBlockEntity(BlockPos pos, BlockState blockState) {
        this(RiverRace.TRIVIA_CHEST_BLOCK_ENTITY.get(), pos, blockState);
    }


    @Override
    public Component getName() {
        return Component.translatable(LoveTropics.ID + ".container.triviaChest");
    }

    @Override
    protected Component getDefaultName() {
        return getName();
    }


    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if(tag.contains(TriviaBlockEntity.TAG_QUESTION)) {
            TriviaBehaviour.TriviaQuestion.CODEC.parse(NbtOps.INSTANCE, tag.get(TriviaBlockEntity.TAG_QUESTION))
                    .resultOrPartial(LOGGER::error)
                    .ifPresent(q -> question = q);
        }
        if(tag.contains(TriviaBlockEntity.TAG_UNLOCKS_AT)) {
            unlocksAt = tag.getLong(TriviaBlockEntity.TAG_UNLOCKS_AT);
        }
        if (tag.contains(TriviaBlockEntity.TAG_ANSWERED)) {
            answered = tag.getBoolean(TriviaBlockEntity.TAG_ANSWERED);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if(question != null) {
            tag.put(TriviaBlockEntity.TAG_QUESTION, TriviaBehaviour.TriviaQuestion.CODEC.encodeStart(NbtOps.INSTANCE, question).getOrThrow());
        }
        if(unlocksAt > 0){
            tag.putLong(TriviaBlockEntity.TAG_UNLOCKS_AT, unlocksAt);
        }
        tag.putBoolean(TriviaBlockEntity.TAG_ANSWERED, answered);
    }

    @Override
    public boolean hasQuestion() {
        return question != null;
    }
    private void markUpdated() {
        setChanged();
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    @Override
    public void setQuestion(TriviaBehaviour.TriviaQuestion question) {
        this.question = question;
        markUpdated();
    }

    @Override
    public TriviaBehaviour.TriviaQuestion getQuestion() {
        return question;
    }

    @Override
    public TriviaBlock.TriviaType getTriviaType() {
        return TriviaBlock.TriviaType.REWARD;
    }

    @Override
    public long lockout(int lockoutSeconds) {
        unlocksAt = System.currentTimeMillis() + (lockoutSeconds * 1000L);
        markUpdated();
        return unlocksAt;
    }

    @Override
    public void unlock() {
        unlocksAt = 0;
        markUpdated();
    }

    @Override
    public void markAsCorrect() {
        answered = true;
        markUpdated();
    }

    @Override
    public TriviaBlockEntity.TriviaBlockState getState() {
        Optional<String> correctAnswer = Optional.empty();
        if(answered){
            correctAnswer = Optional.of(question.answers().stream().filter(TriviaBehaviour.TriviaQuestion.TriviaQuestionAnswer::correct).findFirst().get().text());
        }
        return new TriviaBlockEntity.TriviaBlockState(answered, correctAnswer, unlocksAt > 0, unlocksAt);
    }
}
