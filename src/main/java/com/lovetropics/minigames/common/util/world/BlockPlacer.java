package com.lovetropics.minigames.common.util.world;

import com.google.common.collect.Lists;
import com.lovetropics.lib.BlockBox;
import com.lovetropics.minigames.common.core.game.util.GameScheduler;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.commands.FillCommand;
import net.minecraft.server.commands.SetBlockCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class BlockPlacer {

    static final BlockInput HOLLOW_CORE = new BlockInput(Blocks.AIR.defaultBlockState(), Collections.emptySet(), (CompoundTag)null);

    public enum Mode {
        REPLACE((p_137433_, p_137434_, p_137435_, p_137436_) -> {
            return p_137435_;
        }),
        OUTLINE((p_137428_, p_137429_, p_137430_, p_137431_) -> {
            return p_137429_.getX() != p_137428_.minX() && p_137429_.getX() != p_137428_.maxX() && p_137429_.getY() != p_137428_.minY() && p_137429_.getY() != p_137428_.maxY() && p_137429_.getZ() != p_137428_.minZ() && p_137429_.getZ() != p_137428_.maxZ() ? null : p_137430_;
        }),
        HOLLOW((p_137423_, p_137424_, p_137425_, p_137426_) -> {
            return p_137424_.getX() != p_137423_.minX() && p_137424_.getX() != p_137423_.maxX() && p_137424_.getY() != p_137423_.minY() && p_137424_.getY() != p_137423_.maxY() && p_137424_.getZ() != p_137423_.minZ() && p_137424_.getZ() != p_137423_.maxZ() ? HOLLOW_CORE : p_137425_;
        }),
        DESTROY((p_137418_, p_137419_, p_137420_, p_137421_) -> {
            p_137421_.destroyBlock(p_137419_, true);
            return p_137420_;
        }),
        DESTROY_NO_DROP((p_137418_, p_137419_, p_137420_, p_137421_) -> {
            p_137421_.destroyBlock(p_137419_, false);
            return p_137420_;
        });

        public final SetBlockCommand.Filter filter;

        Mode(SetBlockCommand.Filter pFilter) {
            this.filter = pFilter;
        }
    }

    public static void replace(ServerLevel level, BlockBox box, Block newBlock, Mode mode, Block replacingBlock) {
        placeBlocks(level, box, getBlockInput(newBlock), mode, (blockInWorld -> blockInWorld.getState().getBlock() == replacingBlock), null, null, null);
    }

    public static void replace(ServerLevel level, BlockBox box, Block newBlock, Mode mode, Block replacingBlock, GameScheduler scheduler, Function<BlockPos, Integer> tickDelay) {
        placeBlocks(level, box, getBlockInput(newBlock), mode, (blockInWorld -> blockInWorld.getState().getBlock() == replacingBlock), scheduler, tickDelay, null);
    }

    public static void replace(ServerLevel level, BlockBox box, Block newBlock, Mode mode, Block replacingBlock, GameScheduler scheduler, Function<BlockPos, Integer> tickDelay, Consumer<BlockPos> doneCallback) {
        placeBlocks(level, box, getBlockInput(newBlock), mode, (blockInWorld -> blockInWorld.getState().getBlock() == replacingBlock), scheduler, tickDelay, doneCallback);
    }

    public static void replace(ServerLevel level, BlockBox box, BlockInput newBlock, Mode mode, Block replacingBlock) {
        placeBlocks(level, box, newBlock, mode, (blockInWorld -> blockInWorld.getState().getBlock() == replacingBlock), null, null, null);
    }

    public static BlockInput getBlockInput(Block block) {
        return new BlockInput(block.defaultBlockState(), Collections.emptySet(), null);
    }

    public static void fill(ServerLevel level, BlockBox box, BlockInput newBlock, Mode mode) {
        placeBlocks(level, box, newBlock, mode, null, null, null, null);
    }

    /**
     * Logic mostly copied from the fill command.
     */
    public static void placeBlocks(ServerLevel level, BlockBox box, BlockInput newBlock, Mode mode, Predicate<BlockInWorld> replacingPredicate, @Nullable GameScheduler scheduler, @Nullable Function<BlockPos, Integer> tickDelay, @Nullable Consumer<BlockPos> doneCallback) {

        List<BlockPos> list = Lists.newArrayList();

        BoundingBox boundingBox = new BoundingBox(box.min().getX(), box.min().getY(), box.min().getZ(), box.max().getX(), box.max().getY(), box.max().getZ());
        for (BlockPos blockpos : box) {
            if (replacingPredicate == null || replacingPredicate.test(new BlockInWorld(level, blockpos, true))) {

                if (scheduler != null) {
                    var delay = tickDelay != null ? tickDelay.apply(blockpos) : 0;
                    var blockPosCopy = blockpos.mutable();
                    scheduler.delayedTickEvent("placeBlocks", () -> {
                        BlockInput blockinput = mode.filter.filter(boundingBox, blockPosCopy, newBlock, level);
                        if (blockinput != null) {
                            BlockEntity blockentity = level.getBlockEntity(blockPosCopy);
                            Clearable.tryClear(blockentity);
                            if (blockinput.place(level, blockPosCopy, 2)) {
                                Block block = level.getBlockState(blockPosCopy).getBlock();
                                level.blockUpdated(blockPosCopy, block);
                            }
                        }
                        if (doneCallback != null) {
                            doneCallback.accept(blockPosCopy);
                        }
                    }, delay);
                } else {
                    BlockInput blockinput = mode.filter.filter(boundingBox, blockpos, newBlock, level);
                    if (blockinput != null) {
                        BlockEntity blockentity = level.getBlockEntity(blockpos);
                        Clearable.tryClear(blockentity);
                        if (blockinput.place(level, blockpos, 2)) {
                            list.add(blockpos.immutable());
                        }
                    }
                }
            }
        }

        if(scheduler == null) {
            for(BlockPos blockpos1 : list) {
                Block block = level.getBlockState(blockpos1).getBlock();
                level.blockUpdated(blockpos1, block);
            }
        }
    }
}
