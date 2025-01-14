package com.lovetropics.minigames.common.content.river_race.block;

import com.lovetropics.minigames.common.content.river_race.behaviour.TriviaBehaviour;
import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.Connection;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

import javax.annotation.Nullable;

public class TriviaBlockEntity extends BlockEntity implements HasTrivia {

    public record TriviaBlockState(boolean isAnswered, long unlocksAt){
        public static final StreamCodec<ByteBuf, TriviaBlockState> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL, TriviaBlockState::isAnswered,
                ByteBufCodecs.VAR_LONG, TriviaBlockState::unlocksAt,
                TriviaBlockState::new
        );

        public boolean lockedOut() {
            return unlocksAt > 0;
        }
    }
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String TAG_QUESTION = "question";
    public static final String TAG_UNLOCKS_AT = "unlocksAt";
    @Nullable
    private TriviaBehaviour.TriviaQuestion question;
    private long unlocksAt;
    private final TriviaType triviaType;

    public TriviaBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        if (blockState.getBlock() instanceof TriviaBlock triviaBlock) {
            triviaType = triviaBlock.getType();
        } else {
            throw new IllegalArgumentException("Cannot create TriviaBlockEntity for unrecognised block type: " + blockState);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (question != null) {
            tag.put(TAG_QUESTION, TriviaBehaviour.TriviaQuestion.CODEC.encodeStart(NbtOps.INSTANCE, question).getOrThrow());
        }
        if (unlocksAt > 0) {
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
        return triviaType;
    }

    @Override
	public long lockout(int lockoutSeconds){
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
		if (isAnswered()) {
            return false;
        }
        level.setBlockAndUpdate(getBlockPos(), getBlockState().setValue(TriviaBlock.ANSWERED, true));
        markUpdated();
        return true;
    }

    @Override
    public boolean isAnswered() {
        return getBlockState().getValue(TriviaBlock.ANSWERED);
    }

    @Override
    public TriviaBlockState getState() {
        return new TriviaBlockState(isAnswered(), unlocksAt);
    }

}
