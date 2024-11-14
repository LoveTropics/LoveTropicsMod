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

import javax.annotation.Nullable;
import java.util.Optional;

public class TriviaChestBlockEntity extends ChestBlockEntity implements HasTrivia {
    private static final Logger LOGGER = LogUtils.getLogger();
    @Nullable
    private TriviaBehaviour.TriviaQuestion question;
    private long unlocksAt;

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
    @Nullable
    public TriviaBehaviour.TriviaQuestion getQuestion() {
        return question;
    }

    @Override
    public TriviaType getTriviaType() {
        return TriviaType.REWARD;
    }

    @Override
    public long lockout(int lockoutSeconds) {
        unlocksAt = level.getGameTime() + (lockoutSeconds * 20L);
        markUpdated();
        return unlocksAt;
    }

    @Override
    public void unlock() {
        unlocksAt = 0;
        markUpdated();
    }

    @Override
    public boolean markAsCorrect() {
        if (getBlockState().getValue(TriviaBlock.ANSWERED)) {
            return false;
        }
        level.setBlockAndUpdate(getBlockPos(), getBlockState().setValue(TriviaBlock.ANSWERED, true));
        markUpdated();
        return true;
    }

    @Override
    public TriviaBlockEntity.TriviaBlockState getState() {
        boolean answered = getBlockState().getValue(TriviaBlock.ANSWERED);
        Optional<String> correctAnswer = Optional.empty();
        if (answered) {
            correctAnswer = Optional.of(question.answers().stream().filter(TriviaBehaviour.TriviaQuestion.TriviaQuestionAnswer::correct).findFirst().get().text());
        }
        return new TriviaBlockEntity.TriviaBlockState(answered, correctAnswer, unlocksAt > 0, unlocksAt);
    }
}
