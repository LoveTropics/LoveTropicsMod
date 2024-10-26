package com.lovetropics.minigames.common.content.river_race.block;

import com.lovetropics.minigames.common.content.river_race.RiverRace;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TriviaChestBlock extends ChestBlock {
    public TriviaChestBlock(Properties properties) {
        super(properties, RiverRace.TRIVIA_CHEST_BLOCK_ENTITY::get);
    }
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TriviaChestBlockEntity(pos, state);
    }
}
