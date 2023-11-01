package com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.plant;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.minigames.common.content.biodiversity_blitz.BiodiversityBlitz;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.PlotsState;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.Plant;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.PlantType;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLivingEntityEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

public class MushroomPlantBehavior implements IGameBehavior {
    public static final MapCodec<MushroomPlantBehavior> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            PlantType.CODEC.fieldOf("id").forGetter(c -> c.plantType)
    ).apply(instance, MushroomPlantBehavior::new));
    private final PlantType plantType;

    public MushroomPlantBehavior(PlantType plantType) {
        this.plantType = plantType;
    }

    @Override
    public void register(IGamePhase game, EventRegistrar events) throws GameException {
        events.listen(GameLivingEntityEvents.MOB_DROP, (e, d, r) -> {
            Plot plot = game.getState().getOrThrow(PlotsState.KEY).getPlotAt(e.blockPosition());
            BlockPos p = e.blockPosition();
             BlockBox b = new BlockBox(p.offset(-2, -2, -2), p.offset(2, 2, 2));
            for (BlockPos pos : b) {
                Plant plant = plot.plants.getPlantAt(pos);

                // Mushrooms will cause nearby dying entities to drop extra loot
                if (plant != null && this.plantType.equals(plant.type())) {
                    // TODO: extra bonus in shade
                   r.add(new ItemEntity(e.level(), e.getX(), e.getY(), e.getZ(), new ItemStack(BiodiversityBlitz.OSA_POINT, 1)));
                    break;
                }
            }

            return InteractionResult.PASS;
        });
    }
}
