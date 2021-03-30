package com.lovetropics.minigames.common.core.game.behavior.instances.donation;

import com.google.common.collect.Lists;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.behavior.IGamePackageBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLifecycleEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePackageEvents;
import com.lovetropics.minigames.common.core.integration.game_actions.GamePackage;
import com.lovetropics.minigames.common.core.map.MapRegion;
import com.lovetropics.minigames.common.core.map.MapRegions;
import com.lovetropics.minigames.common.util.Util;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.Heightmap;

import java.util.List;

/**
 * Spawns an amount of entities over a set amount of ticks, spread randomly across all the given regions
 */

public class SpawnEntitiesAtRegionsOverTimePackageBehavior implements IGamePackageBehavior
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
	public void register(IGameInstance game, GameEventListeners events) throws GameException {
		MapRegions regions = game.getMapRegions();

		regionsToSpawnAt.clear();

		for (String key : regionsToSpawnAtKeys) {
			regionsToSpawnAt.addAll(regions.get(key));
		}

		events.listen(GamePackageEvents.RECEIVE_PACKAGE, this::onGamePackageReceived);
		events.listen(GameLifecycleEvents.TICK, this::tick);
	}

	private boolean onGamePackageReceived(final IGameInstance game, final GamePackage gamePackage) {
		if (gamePackage.getPackageType().equals(data.packageType)) {
			ticksRemaining += ticksToSpawnFor;
			entityCountRemaining += entityCount;

			data.onReceive(game, null, gamePackage.getSendingPlayerName());

			return true;
		}

		return false;
	}

	private void tick(IGameInstance game) {
		if (ticksRemaining > 0) {

			//TODO: support less than 1 spawned per tick rate
			//might have a few left unspawned if the rate is real high and not perfectly divisible, but fine for lightning storm
			int spawnsPerTick = Math.max(1, entityCountRemaining / ticksRemaining);

			//System.out.println("spawnsPerTick: " + spawnsPerTick + ", ticksRemaining: " + ticksRemaining);

			for (int i = 0; i < spawnsPerTick; i++) {
				MapRegion region = regionsToSpawnAt.get(game.getWorld().getRandom().nextInt(regionsToSpawnAt.size()));
				final BlockPos pos = game.getWorld().getHeight(Heightmap.Type.WORLD_SURFACE, region.sample(game.getWorld().getRandom()));

				Util.spawnEntity(entityId, game.getWorld(), pos.getX(), pos.getY(), pos.getZ());
				entityCountRemaining--;
			}

			ticksRemaining--;
		}
	}
}
