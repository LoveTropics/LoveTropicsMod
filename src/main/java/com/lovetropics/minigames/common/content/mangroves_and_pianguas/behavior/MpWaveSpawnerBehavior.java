package com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.content.MinigameTexts;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.entity.MpHuskEntity;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.entity.MpPillagerEntity;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.Plot;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.PlotsState;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.Random;

public final class MpWaveSpawnerBehavior implements IGameBehavior {
	public static final Codec<MpWaveSpawnerBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.LONG.fieldOf("wave_interval_seconds").forGetter(c -> c.waveIntervalTicks / 20),
				Codec.LONG.fieldOf("wave_warn_seconds").forGetter(c -> c.waveWarnTicks / 20),
				MoreCodecs.object2Float(MoreCodecs.DIFFICULTY).fieldOf("difficulty_factors").forGetter(c -> c.difficultyFactors)
		).apply(instance, MpWaveSpawnerBehavior::new);
	});

	private final long waveIntervalTicks;
	private final long waveWarnTicks;

	private final Object2FloatMap<Difficulty> difficultyFactors;

	private IGamePhase game;
	private PlotsState plots;

	private int sentWaves = 0;

	public MpWaveSpawnerBehavior(long waveIntervalSeconds, long waveWarnSeconds, Object2FloatMap<Difficulty> difficultyFactors) {
		this.waveIntervalTicks = waveIntervalSeconds * 20;
		this.waveWarnTicks = waveWarnSeconds * 20;

		this.difficultyFactors = difficultyFactors;
		this.difficultyFactors.defaultReturnValue(1.0F);
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		this.game = game;
		this.plots = game.getState().getOrThrow(PlotsState.KEY);

		events.listen(GamePhaseEvents.TICK, this::tick);
	}

	private void tick() {
		ServerWorld world = game.getWorld();
		Random random = world.getRandom();
		long ticks = game.ticks();

		// Spawn mobs every 2 minutes (in daytime)
		long timeTilNextWave = ticks % waveIntervalTicks;

		// Warn players of an impending wave
		// Idea: upgrade that allows you to predict waves in the future?
		if (timeTilNextWave == waveIntervalTicks - waveWarnTicks) {
			game.getParticipants().sendMessage(MinigameTexts.mpWaveWarning());
		}

		if (timeTilNextWave == 0) {
			for (ServerPlayerEntity player : game.getParticipants()) {
				Plot plot = plots.getPlotFor(player);
				if (plot == null) continue;

				this.spawnWave(world, random, plot);
			}

			this.sentWaves++;
		}
	}

	private void spawnWave(ServerWorld world, Random random, Plot plot) {
		Difficulty difficulty = world.getDifficulty();
		float difficultyFactor = difficultyFactors.getFloat(difficulty);

		// Temp wave scaling equation- seems to work fine?
		int x = this.sentWaves / 2;
		int amount = MathHelper.floor(difficultyFactor * (Math.pow(x, 1.2) + x) + 2 + random.nextInt(3));

		for (int i = 0; i < amount; i++) {
			BlockPos pos = plot.mobSpawn.sample(random);

			MobEntity entity = selectEntityForWave(random, world, plot);

			Direction direction = plot.forward.getOpposite();
			entity.setLocationAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, direction.getHorizontalAngle(), 0);

			world.addEntity(entity);

			entity.onInitialSpawn(world, world.getDifficultyForLocation(pos), SpawnReason.MOB_SUMMONED, null, null);
		}
	}

	// TODO: data-drive
	private static MobEntity selectEntityForWave(Random random, World world, Plot plot) {
		if (random.nextBoolean()) {
			return new MpPillagerEntity(EntityType.PILLAGER, world, plot.walls);
		} else {
			return new MpHuskEntity(EntityType.HUSK, world, plot.walls);
		}
	}
}
