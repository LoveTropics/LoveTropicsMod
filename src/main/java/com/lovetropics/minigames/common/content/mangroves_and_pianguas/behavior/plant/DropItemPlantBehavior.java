package com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.plant;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.event.MpEvents;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.Plot;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.plant.Plant;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public final class DropItemPlantBehavior implements IGameBehavior {
    public static final Codec<DropItemPlantBehavior> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MoreCodecs.ITEM_STACK.fieldOf("item").forGetter(b -> b.item),
            Codec.INT.fieldOf("interval").forGetter(b -> b.interval)
    ).apply(instance, DropItemPlantBehavior::new));
    private final ItemStack item;
    private final int interval;

    private IGamePhase game;

    public DropItemPlantBehavior(ItemStack item, int interval) {
        this.item = item;
        this.interval = interval;
    }

    @Override
    public void register(IGamePhase game, EventRegistrar events) throws GameException {
        this.game = game;
        events.listen(MpEvents.TICK_PLANTS, this::tickPlants);
    }

    private void tickPlants(ServerPlayerEntity player, Plot plot, List<Plant> plants) {
        long ticks = this.game.ticks();
        Random random = this.game.getWorld().getRandom();

        if (ticks % this.interval != 0) {
            return;
        }

        ServerWorld world = this.game.getWorld();

        for (Plant plant : plants) {
            BlockPos.Mutable pos = plant.coverage().random(random).toMutable();

            System.out.println(pos);
            for (int i = 0; i < 8; i++) {
                if (world.getBlockState(pos).isAir()) {
                    System.out.println("!! " + pos);
                    world.addEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), this.item.copy()));
                    break;
                }

                pos.move(Direction.DOWN);
            }
        }
    }
}