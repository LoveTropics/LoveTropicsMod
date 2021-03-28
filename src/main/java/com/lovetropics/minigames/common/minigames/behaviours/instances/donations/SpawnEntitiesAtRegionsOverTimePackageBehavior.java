package com.lovetropics.minigames.common.minigames.behaviours.instances.donations;

import com.google.common.collect.Lists;
import com.lovetropics.minigames.common.Util;
import com.lovetropics.minigames.common.game_actions.GamePackage;
import com.lovetropics.minigames.common.map.MapRegion;
import com.lovetropics.minigames.common.map.MapRegions;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigamePackageBehavior;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;

import java.util.List;

/**
 * Spawns an amount of entities over a set amount of ticks, spread randomly across all the given regions
 */

public class SpawnEntitiesAtRegionsOverTimePackageBehavior implements IMinigamePackageBehavior
{
	public static final Codec<SpawnEntitiesAtRegionsOverTimePackageBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				DonationPackageData.CODEC.forGetter(c -> c.data),
				Codec.STRING.listOf().fieldOf("regions_to_spawn_at").forGetter(c -> c.regionsToSpawnAtKeys),
				Registry.ENTITY_TYPE.fieldOf("entity_id").forGetter(c -> c.entityId),
				Codec.INT.optionalFieldOf("entity_count", 1).forGetter(c -> c.entityCount),
				Codec.INT.optionalFieldOf("ticks_to_spawn_for", 1).forGetter(c -> c.ticksToSpawnFor)
		).apply(instance, SpawnEntitiesAtRegionsOverTimePackageBehavior::new);
	});

	private final DonationPackageData data;
	private final List<String> regionsToSpawnAtKeys;
	private final EntityType<?> entityId;
	private final int entityCount;
	private final int ticksToSpawnFor;

	//runtime adjusted vars
	private int ticksRemaining;
	private int entityCountRemaining;

	private final List<MapRegion> regionsToSpawnAt = Lists.newArrayList();

	public SpawnEntitiesAtRegionsOverTimePackageBehavior(final DonationPackageData data, final List<String> regionsToSpawnAtKeys, final EntityType<?> entityId, final int entityCount, final int ticksToSpawnFor) {
		this.data = data;
		this.regionsToSpawnAtKeys = regionsToSpawnAtKeys;
		this.entityId = entityId;
		this.entityCount = entityCount;
		this.ticksToSpawnFor = ticksToSpawnFor;
	}

	@Override
	public String getPackageType() {
		return data.getPackageType();
	}

	@Override
	public void onConstruct(IMinigameInstance minigame) {
		MapRegions regions = minigame.getMapRegions();

		regionsToSpawnAt.clear();

		for (String key : regionsToSpawnAtKeys) {
			regionsToSpawnAt.addAll(regions.get(key));
		}
	}

	@Override
	public boolean onGamePackageReceived(final IMinigameInstance minigame, final GamePackage gamePackage) {
		if (gamePackage.getPackageType().equals(data.packageType)) {

			ticksRemaining += ticksToSpawnFor;
			entityCountRemaining += entityCount;

			data.onReceive(minigame, null, gamePackage.getSendingPlayerName());

			return true;
		}

		return false;
	}

	@Override
	public void worldUpdate(IMinigameInstance minigame, ServerWorld world) {
		if (ticksRemaining > 0) {

			//TODO: support less than 1 spawned per tick rate
			//might have a few left unspawned if the rate is real high and not perfectly divisible, but fine for lightning storm
			int spawnsPerTick = Math.max(1, entityCountRemaining / ticksRemaining);

			//System.out.println("spawnsPerTick: " + spawnsPerTick + ", ticksRemaining: " + ticksRemaining);

			for (int i = 0; i < spawnsPerTick; i++) {
				MapRegion region = regionsToSpawnAt.get(minigame.getWorld().getRandom().nextInt(regionsToSpawnAt.size()));
				final BlockPos pos = minigame.getWorld().getHeight(Heightmap.Type.WORLD_SURFACE, region.sample(minigame.getWorld().getRandom()));

				Util.spawnEntity(entityId, minigame.getWorld(), pos.getX(), pos.getY(), pos.getZ());
				entityCountRemaining--;
			}

			ticksRemaining--;
		}
	}
}
