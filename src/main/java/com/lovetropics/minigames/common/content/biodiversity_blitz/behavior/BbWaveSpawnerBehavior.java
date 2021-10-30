package com.lovetropics.minigames.common.content.biodiversity_blitz.behavior;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.content.biodiversity_blitz.BiodiversityBlitzTexts;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event.BbEvents;
import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.BbHuskEntity;
import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.BbPillagerEntity;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.PlotsState;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfo.Color;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerBossInfo;
import net.minecraft.world.server.ServerWorld;

import java.util.*;

public final class BbWaveSpawnerBehavior implements IGameBehavior {
	public static final Codec<BbWaveSpawnerBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.LONG.fieldOf("interval_seconds").forGetter(c -> c.intervalTicks / 20),
				Codec.LONG.fieldOf("warn_seconds").forGetter(c -> c.warnTicks / 20),
				SizeCurve.CODEC.fieldOf("size_curve").forGetter(c -> c.sizeCurve),
				MoreCodecs.object2Float(MoreCodecs.DIFFICULTY).fieldOf("difficulty_factors").forGetter(c -> c.difficultyFactors)
		).apply(instance, BbWaveSpawnerBehavior::new);
	});

	private final long intervalTicks;
	private final long warnTicks;
	private final SizeCurve sizeCurve;

	private final Object2FloatMap<Difficulty> difficultyFactors;

	private IGamePhase game;
	private PlotsState plots;

	private int sentWaves = 0;
	private Map<UUID, List<WaveTracker>> waveTrackers = new HashMap<>();
	private ServerBossInfo waveCharging;

	public BbWaveSpawnerBehavior(long intervalSeconds, long warnSeconds, SizeCurve sizeCurve, Object2FloatMap<Difficulty> difficultyFactors) {
		this.intervalTicks = intervalSeconds * 20;
		this.warnTicks = warnSeconds * 20;
		this.sizeCurve = sizeCurve;

		this.difficultyFactors = difficultyFactors;
		this.difficultyFactors.defaultReturnValue(1.0F);
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		this.game = game;
		this.plots = game.getState().getOrThrow(PlotsState.KEY);

		events.listen(BbEvents.ASSIGN_PLOT, this::addPlayer);
		events.listen(GamePlayerEvents.REMOVE, this::removePlayer);

		events.listen(GamePhaseEvents.TICK, this::tick);
		events.listen(GamePhaseEvents.STOP, reason -> {
			waveTrackers.values().stream()
					.flatMap(List::stream)
					.forEach(WaveTracker::close);
			cleanupBossBar(waveCharging);
		});

		this.waveCharging = new ServerBossInfo(new StringTextComponent("Wave Incoming!"), Color.GREEN, BossInfo.Overlay.PROGRESS);
		this.waveCharging.setPercent(0.0F);
		this.waveCharging.setVisible(false);
	}

	private void addPlayer(ServerPlayerEntity player, Plot plot) {
		this.waveCharging.addPlayer(player);
	}

	private void removePlayer(ServerPlayerEntity player) {
		this.waveCharging.removePlayer(player);
		List<WaveTracker> waves = this.waveTrackers.remove(player.getUniqueID());
		if (waves != null) {
			for (WaveTracker wave : waves) {
				wave.close();
			}
		}
	}

	private void tick() {
		ServerWorld world = game.getWorld();
		Random random = world.getRandom();
		long ticks = game.ticks();

		long timeTilNextWave = ticks % intervalTicks;

		if (timeTilNextWave == intervalTicks - warnTicks) {
			game.getParticipants().sendMessage(BiodiversityBlitzTexts.waveWarning(), true);
		}

		if (timeTilNextWave > intervalTicks - 40) {
			this.waveCharging.setPercent(1.0F - (intervalTicks - timeTilNextWave) / 40.0F);
			this.waveCharging.setVisible(true);
		}

		if (timeTilNextWave == 0) {
			this.waveCharging.setVisible(false);
			for (ServerPlayerEntity player : game.getParticipants()) {
				Plot plot = plots.getPlotFor(player);
				if (plot == null) continue;

				this.spawnWave(world, random, player, plot, sentWaves);
			}

			this.sentWaves++;
		}

		this.waveTrackers.forEach((pid, waves) -> {
			Iterator<WaveTracker> iterator = waves.iterator();
			while (iterator.hasNext()) {
				WaveTracker wave = iterator.next();
				if (this.tickWave(wave)) {
					wave.close();
					iterator.remove();
				}
			}
		});
	}

	private boolean tickWave(WaveTracker wave) {
		if (wave.entities.removeIf(e -> !e.isAlive())) {
			wave.bar.setPercent((float) wave.entities.size() / wave.waveSize);
		}
		return wave.entities.isEmpty();
	}

	private void cleanupBossBar(ServerBossInfo bar) {
		bar.removeAllPlayers();
	}

	private void spawnWave(ServerWorld world, Random random, ServerPlayerEntity player, Plot plot, int waveIndex) {
		Difficulty difficulty = world.getDifficulty();
		float difficultyFactor = difficultyFactors.getFloat(difficulty);

		double fractionalCount = sizeCurve.apply(waveIndex, difficultyFactor);
		int count = MathHelper.floor(fractionalCount);
		if (random.nextDouble() > fractionalCount - count) {
			count++;
		}

		Set<Entity> entities = this.spawnWaveEntities(world, random, plot, count);
		ServerBossInfo bossBar = this.createWaveBar(player, waveIndex, count, entities);

		WaveTracker wave = new WaveTracker(bossBar, entities);
		waveTrackers.computeIfAbsent(player.getUniqueID(), $ -> new ArrayList<>()).add(wave);
	}

	private Set<Entity> spawnWaveEntities(ServerWorld world, Random random, Plot plot, int count) {
		Set<Entity> entities = Collections.newSetFromMap(new WeakHashMap<>());
		for (int i = 0; i < count; i++) {
			BlockPos pos = plot.mobSpawn.sample(random);

			MobEntity entity = selectEntityForWave(random, world, plot);

			Direction direction = plot.forward.getOpposite();
			entity.setLocationAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, direction.getHorizontalAngle(), 0);

			world.addEntity(entity);

			entity.onInitialSpawn(world, world.getDifficultyForLocation(pos), SpawnReason.MOB_SUMMONED, null, null);
			entities.add(entity);
		}

		return entities;
	}

	private ServerBossInfo createWaveBar(ServerPlayerEntity player, int waveIndex, int count, Set<Entity> entities) {
		ServerBossInfo bossBar = new ServerBossInfo(new StringTextComponent("Wave " + (waveIndex + 1)), Color.GREEN, BossInfo.Overlay.PROGRESS);
		bossBar.setPercent((float) entities.size() / count);
		bossBar.setColor(Color.GREEN);
		bossBar.addPlayer(player);

		return bossBar;
	}

	// TODO: data-drive, more entity types & getting harder as time goes on
	private static MobEntity selectEntityForWave(Random random, World world, Plot plot) {
		if (random.nextBoolean()) {
			return new BbPillagerEntity(EntityType.PILLAGER, world, plot);
		} else {
			return new BbHuskEntity(EntityType.HUSK, world, plot);
		}
	}

	static final class WaveTracker {
		final ServerBossInfo bar;
		final Set<Entity> entities;
		final int waveSize;

		WaveTracker(ServerBossInfo bar, Set<Entity> entities) {
			this.bar = bar;
			this.entities = entities;
			this.waveSize = entities.size();
		}

		void close() {
			bar.removeAllPlayers();
		}
	}

	static final class SizeCurve {
		public static final Codec<SizeCurve> CODEC = RecordCodecBuilder.create(instance -> {
			return instance.group(
					Codec.DOUBLE.fieldOf("lower").forGetter(c -> c.lower),
					Codec.DOUBLE.fieldOf("upper").forGetter(c -> c.upper),
					Codec.DOUBLE.fieldOf("base").forGetter(c -> c.base),
					Codec.DOUBLE.fieldOf("scale").forGetter(c -> c.scale)
			).apply(instance, SizeCurve::new);
		});

		final double lower;
		final double upper;
		final double base;
		final double scale;

		SizeCurve(double lower, double upper, double base, double scale) {
			this.lower = lower;
			this.upper = upper;
			this.base = base;
			this.scale = scale;
		}

		double apply(int index, float difficulty) {
			double lower = this.lower * difficulty;
			double range = this.upper - lower;
			double base = this.base * difficulty;
			double scale = this.scale;
			return lower + range * (1.0 - Math.pow(base, -index * scale / range));
		}
	}
}
