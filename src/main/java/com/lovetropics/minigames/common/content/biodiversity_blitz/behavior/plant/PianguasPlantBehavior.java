package com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.plant;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event.BbPlantEvents;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.Plant;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.Tags;

import java.util.List;
import java.util.Random;

public final class PianguasPlantBehavior implements IGameBehavior {
    public static final Codec<PianguasPlantBehavior> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("radius").forGetter(b -> b.radius),
            MoreCodecs.BLOCK_STATE.fieldOf("block").forGetter(c -> c.state)
    ).apply(instance, PianguasPlantBehavior::new));
    private static final Tags.IOptionalNamedTag<Block> MUD = BlockTags.createOptional(new ResourceLocation("tropicraft", "mud"));
    private final int radius;
    private final BlockState state;

    private IGamePhase game;

    public PianguasPlantBehavior(int radius, BlockState state) {
        this.radius = radius;
        this.state = state;
    }

    @Override
    public void register(IGamePhase game, EventRegistrar events) throws GameException {
        this.game = game;
        events.listen(BbPlantEvents.TICK, this::tickPlants);
    }

    private void tickPlants(ServerPlayerEntity player, Plot plot, List<Plant> plants) {
        long ticks = this.game.ticks();
        Random random = this.game.getWorld().getRandom();

        // TODO: rebalance
        if (ticks % 300 != 0) {
            return;
        }

        ServerWorld world = this.game.getWorld();

        for (Plant plant : plants) {
            int dx = random.nextInt(this.radius) - random.nextInt(this.radius);
            int dz = random.nextInt(this.radius) - random.nextInt(this.radius);

            BlockPos check = plant.coverage().getOrigin().add(dx, -1, dz);

            if (world.getBlockState(check).isIn(MUD)) {
                world.setBlockState(check, state);
            }
        }
    }
}
