package com.lovetropics.minigames.common.content.biodiversity_blitz.behavior;

import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.BbCreeperEntity;
import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.BbMobSpawner;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.PlotsState;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePackageEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.world.World;

import java.util.Random;

public class SpawnSurpriseWaveBehavior implements IGameBehavior {
    public static final Codec<SpawnSurpriseWaveBehavior> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("wave_size").forGetter(b -> b.waveSize)
    ).apply(instance, SpawnSurpriseWaveBehavior::new));
    private final int waveSize;
    private IGamePhase game;
    private PlotsState plots;

    public SpawnSurpriseWaveBehavior(int waveSize) {
        this.waveSize = waveSize;
    }

    @Override
    public void register(IGamePhase game, EventRegistrar events) throws GameException {
        this.game = game;
        this.plots = game.getState().getOrThrow(PlotsState.KEY);

        events.listen(GamePackageEvents.APPLY_PACKAGE_TO_PLAYER, (player, sendingPlayer) -> {
            Plot plot = this.plots.getPlotFor(player);

            BbMobSpawner.spawnWaveEntities(player.getLevel(), player.getRandom(), plot, this.waveSize, 0, SpawnSurpriseWaveBehavior::selectEntityForWave);

            return true;
        });
    }

    private static MobEntity selectEntityForWave(Random random, World world, Plot plot, int waveIndex) {
        return new BbCreeperEntity(EntityType.CREEPER, world, plot);
    }
}
