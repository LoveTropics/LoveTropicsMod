package com.lovetropics.minigames.common.content.biodiversity_blitz.behavior;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.content.biodiversity_blitz.BiodiversityBlitzTexts;
import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.BbHuskEntity;
import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.BbPillagerEntity;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.PlotsState;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntStack;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.CustomServerBossInfo;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.BossInfo.Color;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public final class BbWaveSpawnerBehavior implements IGameBehavior {
	public static final Codec<BbWaveSpawnerBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.LONG.fieldOf("wave_interval_seconds").forGetter(c -> c.waveIntervalTicks / 20),
				Codec.LONG.fieldOf("wave_warn_seconds").forGetter(c -> c.waveWarnTicks / 20),
				MoreCodecs.object2Float(MoreCodecs.DIFFICULTY).fieldOf("difficulty_factors").forGetter(c -> c.difficultyFactors)
		).apply(instance, BbWaveSpawnerBehavior::new);
	});

	private final long waveIntervalTicks;
	private final long waveWarnTicks;

	private final Object2FloatMap<Difficulty> difficultyFactors;

	private IGamePhase game;
	private PlotsState plots;

	private int sentWaves = 0;
	private Map<UUID, List<CustomServerBossInfo>> bossBars = new HashMap<>();
	private Map<UUID, List<Set<Entity>>> entitiesInWave = new HashMap<>();
	private CustomServerBossInfo waveCharging;

	public BbWaveSpawnerBehavior(long waveIntervalSeconds, long waveWarnSeconds, Object2FloatMap<Difficulty> difficultyFactors) {
		this.waveIntervalTicks = waveIntervalSeconds * 20;
		this.waveWarnTicks = waveWarnSeconds * 20;

		this.difficultyFactors = difficultyFactors;
		this.difficultyFactors.defaultReturnValue(1.0F);
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		this.game = game;
		this.plots = game.getState().getOrThrow(PlotsState.KEY);

		events.listen(GamePhaseEvents.START, () -> game.getParticipants().forEach(this.waveCharging::addPlayer));
		events.listen(GamePhaseEvents.TICK, this::tick);
		events.listen(GamePhaseEvents.FINISH, () -> {
			bossBars.values().stream()
				.flatMap(List::stream)
				.forEach(this::cleanupBossBar);
			cleanupBossBar(waveCharging);
		});

		this.waveCharging = game.getServer().getCustomBossEvents().add(new ResourceLocation(Constants.MODID, "bb_wave_charging"), new StringTextComponent("Wave Incoming!"));
		this.waveCharging.setColor(Color.GREEN);
		this.waveCharging.setMax(40);
		this.waveCharging.setValue(0);
		this.waveCharging.setVisible(false);
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
			game.getParticipants().sendMessage(BiodiversityBlitzTexts.waveWarning());
		}

		if (timeTilNextWave > waveIntervalTicks - 40) {
			this.waveCharging.setValue((int) (40 - (waveIntervalTicks - timeTilNextWave)));
			this.waveCharging.setVisible(true);
		}

		if (timeTilNextWave == 0) {
			this.waveCharging.setVisible(false);
			for (ServerPlayerEntity player : game.getParticipants()) {
				Plot plot = plots.getPlotFor(player);
				if (plot == null) continue;

				this.spawnWave(world, random, player, plot);
			}

			this.sentWaves++;
		}

		this.entitiesInWave.forEach((pid, waves) -> {
			List<CustomServerBossInfo> bars = bossBars.get(pid);
			
			IntStack toRemove = new IntArrayList();
			for (int i = 0; i < waves.size(); i++) {
				Set<Entity> wave = waves.get(i);
				wave.removeIf(e -> !e.isAlive());
				CustomServerBossInfo bar = bars.get(i);
				if (wave.isEmpty()) {
					toRemove.push(i);
				}
				bar.setValue(wave.size());
			}
			while (!toRemove.isEmpty()) {
				int i = toRemove.popInt();
				cleanupBossBar(bars.remove(i));
				waves.remove(i);
			}
		});
	}

	private void cleanupBossBar(CustomServerBossInfo bar) {
		bar.removeAllPlayers();
		game.getServer().getCustomBossEvents().remove(bar);
	}

	private void spawnWave(ServerWorld world, Random random, ServerPlayerEntity player, Plot plot) {
		Difficulty difficulty = world.getDifficulty();
		float difficultyFactor = difficultyFactors.getFloat(difficulty);

		// Temp wave scaling equation- seems to work fine?
		int x = this.sentWaves / 2;
		int amount = MathHelper.floor(difficultyFactor * (Math.pow(x, 1.2) + x) + 2 + random.nextInt(3));

		Set<Entity> entities = Collections.newSetFromMap(new WeakHashMap<>());
		for (int i = 0; i < amount; i++) {
			BlockPos pos = plot.mobSpawn.sample(random);

			MobEntity entity = selectEntityForWave(random, world, plot);

			Direction direction = plot.forward.getOpposite();
			entity.setLocationAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, direction.getHorizontalAngle(), 0);

			world.addEntity(entity);

			entity.onInitialSpawn(world, world.getDifficultyForLocation(pos), SpawnReason.MOB_SUMMONED, null, null);
			entities.add(entity);
		}

		UUID uuid = player.getUniqueID();
		CustomServerBossInfo bossBar = game.getServer().getCustomBossEvents()
				.add(new ResourceLocation(Constants.MODID, uuid + "_" + this.sentWaves), new StringTextComponent("Wave " + (this.sentWaves + 1)));
		bossBar.setMax(amount);
		bossBar.setValue(entities.size());
		bossBar.setColor(Color.GREEN);
		bossBar.addPlayer(player);

		bossBars.computeIfAbsent(player.getUniqueID(), $ -> new ArrayList<>()).add(bossBar);
		entitiesInWave.computeIfAbsent(uuid, $ -> new ArrayList<>()).add(entities);
	}

	// TODO: data-drive
	private static MobEntity selectEntityForWave(Random random, World world, Plot plot) {
		if (random.nextBoolean()) {
			return new BbPillagerEntity(EntityType.PILLAGER, world, plot);
		} else {
			return new BbHuskEntity(EntityType.HUSK, world, plot);
		}
	}
}
