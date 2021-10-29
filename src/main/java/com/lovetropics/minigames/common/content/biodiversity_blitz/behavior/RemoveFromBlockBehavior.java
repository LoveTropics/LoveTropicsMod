package com.lovetropics.minigames.common.content.biodiversity_blitz.behavior;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.server.ServerWorld;

// Removes pianguas from mud blocks
public final class RemoveFromBlockBehavior implements IGameBehavior {
    public static final Codec<RemoveFromBlockBehavior> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MoreCodecs.BLOCK_STATE.fieldOf("in").forGetter(b -> b.in),
            MoreCodecs.BLOCK_STATE.fieldOf("out").forGetter(b -> b.out),
            MoreCodecs.ITEM_STACK.fieldOf("drop").forGetter(b -> b.drop)
    ).apply(instance, RemoveFromBlockBehavior::new));
    private final BlockState in;
    private final BlockState out;
    private final ItemStack drop;

    public RemoveFromBlockBehavior(BlockState in, BlockState out, ItemStack drop) {
        this.in = in;
        this.out = out;
        this.drop = drop;
    }

    @Override
    public void register(IGamePhase game, EventRegistrar events) throws GameException {
        events.listen(GamePlayerEvents.USE_BLOCK, this::onUseBlock);
    }

    private ActionResultType onUseBlock(ServerPlayerEntity player, ServerWorld world, BlockPos pos, Hand hand, BlockRayTraceResult result) {
        if (world.getBlockState(pos).getBlock() == this.in.getBlock()) {
            world.setBlockState(pos, this.out);

            BlockPos spawnPos = pos.offset(result.getFace());
            world.addEntity(new ItemEntity(world, spawnPos.getX() + 0.5, spawnPos.getY() + 0.5, spawnPos.getZ() + 0.5, this.drop.copy()));

            return ActionResultType.SUCCESS;
        }

        return ActionResultType.PASS;
    }
}
