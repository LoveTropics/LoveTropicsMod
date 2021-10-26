package com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.plant;

import com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.event.MpPlantEvents;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.plant.Plant;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.util.Random;

public abstract class AgingPlantBehavior implements IGameBehavior {
    protected final int interval;

    public AgingPlantBehavior(int interval) {
        this.interval = interval;
    }

    @Override
    public void register(IGamePhase game, EventRegistrar events) {
        events.listen(MpPlantEvents.TICK, (player, plot, plants) -> {
            long ticks = game.ticks();
            if (ticks % this.interval != 0) {
                return;
            }

            ServerWorld world = game.getWorld();

            for (Plant plant : plants) {
                for (BlockPos pos : plant.coverage()) {
                    BlockState state = world.getBlockState(pos);
                    BlockState agedState = ageUp(world.rand, state);

                    if (state != agedState) {
                        plant.spawnPoof(world, 5, 0.01);
                        world.setBlockState(pos, agedState);
                    }
                }
            }
        });
    }

    protected abstract BlockState ageUp(Random random, BlockState state);
}
