package com.lovetropics.minigames.common.core.game.behavior.instances.donation;

import com.google.common.collect.Lists;
import com.lovetropics.lib.BlockBox;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePackageEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.map.MapRegions;
import com.lovetropics.minigames.common.util.Util;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.List;

/**
 * Spawns an amount of entities over a set amount of ticks, spread randomly across all the given regions
 */

public class SpawnEntitiesAtRegionsOverTimePackageBehavior implements IGameBehavior {
	public static final Codec<SpawnEntitiesAtRegionsOverTimePackageBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.STRING.listOf().fieldOf("regions_to_spawn_at").forGetter(c -> c.regionsToSpawnAtKeys),
				Registry.ENTITY_TYPE.fieldOf("entity_id").forGetter(c -> c.entityId),
				Codec.INT.optionalFieldOf("entity_count", 1).forGetter(c -> c.entityCount),
				Codec.INT.optionalFieldOf("ticks_to_spawn_for", 1).forGetter(c -> c.ticksToSpawnFor)
		).apply(instance, SpawnEntitiesAtRegionsOverTimePackageBehavior::new);
	});

	private final List<String> regionsToSpawnAtKeys;
	private final EntityType<?> entityId;
	private final int entityCount;
	private final int ticksToSpawnFor;

	//runtime adjusted vars
	private int ticksRemaining;
	private int entityCountRemaining;

	private final List<BlockBox> regionsToSpawnAt = Lists.newArrayList();

	public SpawnEntitiesAtRegionsOverTimePackageBehavior(final List<String> regionsToSpawnAtKeys, final EntityType<?> entityId, final int entityCount, final int ticksToSpawnFor) {
		this.regionsToSpawnAtKeys = regionsToSpawnAtKeys;
		this.entityId = entityId;
		this.entityCount = entityCount;
		this.ticksToSpawnFor = ticksToSpawnFor;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		MapRegions regions = game.getMapRegions();

		regionsToSpawnAt.clear();
		for (String key : regionsToSpawnAtKeys) {
			regionsToSpawnAt.addAll(regions.get(key));
		}

		events.listen(GamePackageEvents.APPLY_PACKAGE_GLOBALLY, this::applyPackage);
		events.listen(GamePhaseEvents.TICK, () -> tick(game));
	}

	private boolean applyPackage(String senderPlayerName) {
		ticksRemaining += ticksToSpawnFor;
		entityCountRemaining += entityCount;
		return true;
	}

	private void tick(IGamePhase game) {
		if (ticksRemaining > 0) {

			//TODO: support less than 1 spawned per tick rate
			//might have a few left unspawned if the rate is real high and not perfectly divisible, but fine for lightning storm
			int spawnsPerTick = Math.max(1, entityCountRemaining / ticksRemaining);

			//System.out.println("spawnsPerTick: " + spawnsPerTick + ", ticksRemaining: " + ticksRemaining);

			for (int i = 0; i < spawnsPerTick; i++) {
				BlockBox region = regionsToSpawnAt.get(game.getWorld().getRandom().nextInt(regionsToSpawnAt.size()));
				final BlockPos pos = game.getWorld().getHeightmapPos(Heightmap.Types.WORLD_SURFACE, region.sample(game.getWorld().getRandom()));

				Util.spawnEntity(entityId, game.getWorld(), pos.getX(), pos.getY(), pos.getZ());
				entityCountRemaining--;
			}

			ticksRemaining--;
		}
	}
}
