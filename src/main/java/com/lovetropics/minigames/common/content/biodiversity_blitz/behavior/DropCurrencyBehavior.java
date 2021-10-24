package com.lovetropics.minigames.common.content.biodiversity_blitz.behavior;

import com.lovetropics.minigames.common.content.MinigameTexts;
import com.lovetropics.minigames.common.content.biodiversity_blitz.BiodiversityBlitz;
import com.lovetropics.minigames.common.content.biodiversity_blitz.BiodiversityBlitzTexts;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event.BbEvents;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.Plant;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.PlantFamily;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.PlantType;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.mojang.serialization.Codec;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.server.ServerWorld;

import java.util.*;

public final class DropCurrencyBehavior implements IGameBehavior {
    public static final Codec<DropCurrencyBehavior> CODEC = Codec.unit(DropCurrencyBehavior::new);
    private IGamePhase game;

    @Override
    public void register(IGamePhase game, EventRegistrar events) throws GameException {
        this.game = game;
        events.listen(BbEvents.TICK_PLOT, this::tickPlot);
    }

    private void tickPlot(ServerPlayerEntity player, Plot plot) {
        long ticks = this.game.ticks();
        Random random = this.game.getWorld().getRandom();

        if (ticks % 600 != 0) {
            return;
        }

        ServerWorld world = this.game.getWorld();

        Map<PlantFamily, Double> values = new HashMap<>();
        Map<PlantFamily, Set<PlantType>> counts = new HashMap<>();

        for (Plant plant : plot.plants) {
            PlantFamily family = plant.family();
            PlantType type = plant.type();
            double value = plant.value();

            values.put(family, values.getOrDefault(family, 0.0) + value);

            // TODO: plants like saplings should not contribute to biodiversity!
            Set<PlantType> set = counts.getOrDefault(family, new HashSet<>());
            set.add(type);
            counts.put(family, set);
        }

        int count = 2;
        for (PlantFamily family : PlantFamily.values()) {
            Double value = values.get(family);

            if (value == null) {
                // Type was not found
                continue;
            }

            int biodiversity = counts.get(family).size();

            value += 0.5;

            value += (biodiversity / 3.0) * value;

            // Highly cursed but we keep the boxed version to prevent instant NPE
            int rawVal = (int)(double)value;

            if (random.nextDouble() < (value - rawVal)) {
                rawVal++;
            }

            count += rawVal;
        }

        count = preventCapitalism(count);

        // TODO: world.playSound! this isn't sent to the player
        player.playSound(SoundEvents.ENTITY_ARROW_HIT_PLAYER, 0.24F, 1.0F);
        player.sendStatusMessage(BiodiversityBlitzTexts.currencyAddition(count), false);
        player.addItemStackToInventory(new ItemStack(BiodiversityBlitz.OSA_POINT.get(), count));
    }

    private static int preventCapitalism(int count) {
        if (count < 60) {
            return count;
        }

        // \left(60+\frac{x}{20}\right)-1.23^{-\frac{x}{3}}+1
        return (int)((60 + (count / 20.0)) - Math.pow(1.23, -count / 3.0) + 1);
    }
}
