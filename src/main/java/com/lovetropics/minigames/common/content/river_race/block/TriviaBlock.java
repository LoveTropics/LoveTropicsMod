package com.lovetropics.minigames.common.content.river_race.block;

import com.lovetropics.minigames.common.content.river_race.RiverRace;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class TriviaBlock extends Block implements EntityBlock {

    public static class RewardTriviaBlock extends TriviaBlock {
        public RewardTriviaBlock(Properties properties) {
            super(properties, TriviaType.REWARD);
        }
    }
    public static class GateTriviaBlock extends TriviaBlock {
        public GateTriviaBlock(Properties properties) {
            super(properties, TriviaType.GATE);
        }
    }

    public static class CollectableTriviaBlock extends TriviaBlock {
        public CollectableTriviaBlock(Properties properties) {
            super(properties, TriviaType.COLLECTABLE);
        }
    }
    public static class VictoryTriviaBlock extends TriviaBlock {
        public VictoryTriviaBlock(Properties properties) {
            super(properties, TriviaType.VICTORY);
        }
    }

    private final TriviaType type;

    public TriviaBlock(Properties properties, TriviaType blockType) {
        super(properties);
        this.type = blockType;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new TriviaBlockEntity(RiverRace.TRIVIA_BLOCK_ENTITY.get(), blockPos, blockState);
    }

    public TriviaType getType() {
        return type;
    }
}
