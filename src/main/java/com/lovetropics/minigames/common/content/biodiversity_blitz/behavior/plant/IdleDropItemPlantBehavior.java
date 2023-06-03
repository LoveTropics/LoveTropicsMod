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
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.Collection;
import java.util.List;
import java.util.Random;

public final class IdleDropItemPlantBehavior implements IGameBehavior {
    public static final Codec<IdleDropItemPlantBehavior> CODEC = RecordCodecBuilder.create(i -> i.group(
            MoreCodecs.ITEM_STACK.fieldOf("item").forGetter(b -> b.item),
            Codec.INT.fieldOf("interval").forGetter(b -> b.interval)
    ).apply(i, IdleDropItemPlantBehavior::new));
    private final ItemStack item;
    private final int interval;

    private IGamePhase game;

    public IdleDropItemPlantBehavior(ItemStack item, int interval) {
        this.item = item;
        this.interval = interval;
    }

    @Override
    public void register(IGamePhase game, EventRegistrar events) throws GameException {
        this.game = game;
        events.listen(BbPlantEvents.TICK, this::tickPlants);
    }

    private void tickPlants(Collection<ServerPlayer> players, Plot plot, List<Plant> plants) {
        long ticks = this.game.ticks();
        RandomSource random = this.game.getWorld().getRandom();

        if (ticks % this.interval != 0) {
            return;
        }

        ServerLevel world = this.game.getWorld();

        for (Plant plant : plants) {
            BlockPos.MutableBlockPos pos = plant.coverage().random(random).mutable();

            for (int i = 0; i < 8; i++) {
                if (world.getBlockState(pos).isAir()) {
                    world.addFreshEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), this.item.copy()));
                    break;
                }

                pos.move(Direction.DOWN);
            }
        }
    }
}
