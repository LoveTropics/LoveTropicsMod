package com.lovetropics.minigames.common.content.biodiversity_blitz.behavior;

import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.BbCreeperEntity;
import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.BbMobSpawner;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.PlotsState;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameActionEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

import java.util.Random;

public class SpawnSurpriseWaveBehavior implements IGameBehavior {
    public static final Codec<SpawnSurpriseWaveBehavior> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("wave_size").forGetter(b -> b.waveSize)
    ).apply(instance, SpawnSurpriseWaveBehavior::new));
    private final int waveSize;
    private PlotsState plots;

    public SpawnSurpriseWaveBehavior(int waveSize) {
        this.waveSize = waveSize;
    }

    @Override
    public void register(IGamePhase game, EventRegistrar events) throws GameException {
        this.plots = game.getState().getOrThrow(PlotsState.KEY);

        events.listen(GameActionEvents.APPLY_TO_PLAYER, (context, player) -> {
            // FIXME: this will spawn a wave for every player associated to the plot. probably not ideal.
            Plot plot = this.plots.getPlotFor(player);

            BbMobSpawner.spawnWaveEntities(player.getLevel(), player.getRandom(), plot, this.waveSize, 0, SpawnSurpriseWaveBehavior::selectEntityForWave);

            return true;
        });
    }

    private static Mob selectEntityForWave(RandomSource random, Level world, Plot plot, int waveIndex) {
        return new BbCreeperEntity(EntityType.CREEPER, world, plot);
    }
}
