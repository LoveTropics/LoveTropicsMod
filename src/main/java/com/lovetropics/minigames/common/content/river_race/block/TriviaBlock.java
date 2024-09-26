package com.lovetropics.minigames.common.content.river_race.block;

import com.lovetropics.minigames.common.content.river_race.RiverRace;
import com.lovetropics.minigames.common.content.survive_the_tide.block.BigRedButtonBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class TriviaBlock extends Block implements EntityBlock {

    public static class RewardTriviaBlock extends TriviaBlock {
        public RewardTriviaBlock(Properties properties) {
            super(properties, TriviaBlockType.REWARD);
        }
    }
    public static class GateTriviaBlock extends TriviaBlock {
        public GateTriviaBlock(Properties properties) {
            super(properties, TriviaBlockType.GATE);
        }
    }

    public static class CollectableTriviaBlock extends TriviaBlock {
        public CollectableTriviaBlock(Properties properties) {
            super(properties, TriviaBlockType.COLLECTABLE);
        }
    }

    public enum TriviaBlockType implements StringRepresentable {
        REWARD("easy"),
        GATE("medium"),
        COLLECTABLE("hard");

        private String difficulty;
        TriviaBlockType(String difficulty){
            this.difficulty = difficulty;
        }

        public String difficulty() {
            return difficulty;
        }

        @Override
        public String getSerializedName() {
            return toString().toLowerCase();
        }
    }

    private final TriviaBlockType type;

    public TriviaBlock(Properties properties, TriviaBlockType blockType) {
        super(properties);
        this.type = blockType;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new TriviaBlockEntity(RiverRace.TRIVIA_BLOCK_ENTITY.get(), blockPos, blockState)
                .setTriviaType(type);
    }

    public TriviaBlockType getType() {
        return type;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
    }


}
