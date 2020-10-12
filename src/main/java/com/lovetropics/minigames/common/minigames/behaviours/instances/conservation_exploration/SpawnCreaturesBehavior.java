package com.lovetropics.minigames.common.minigames.behaviours.instances.conservation_exploration;

import com.lovetropics.minigames.common.map.MapRegion;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.MinigameBehaviorTypes;
import com.mojang.datafixers.Dynamic;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class SpawnCreaturesBehavior implements IMinigameBehavior {
	private final List<EntitySpawner> entitySpawners;

	public SpawnCreaturesBehavior(List<EntitySpawner> entitySpawners) {
		this.entitySpawners = entitySpawners;
	}

	@Override
	public void onStart(IMinigameInstance minigame) {
		int spawnedCount = spawnEntities(minigame);
		minigame.getBehavior(MinigameBehaviorTypes.RECORD_CREATURES.get()).ifPresent(record -> {
			record.setTotalEntityCount(spawnedCount);
		});
	}

	private int spawnEntities(IMinigameInstance minigame) {
		ServerWorld world = minigame.getWorld();
		Random random = world.getRandom();

		int count = 0;

		for (EntitySpawner spawner : this.entitySpawners) {
			String regionKey = spawner.region;
			List<MapRegion> regions = new ArrayList<>(minigame.getMapRegions().get(regionKey));
			if (regions.isEmpty()) {
				continue;
			}

			EntityType<?> entityType = spawner.entity;
			for (int i = 0; i < spawner.count; i++) {
				MapRegion region = regions.get(random.nextInt(regions.size()));

				BlockPos pos = new BlockPos(
						region.min.getX() + random.nextInt(region.max.getX() - region.min.getX() + 1),
						region.min.getY(),
						region.min.getZ() + random.nextInt(region.max.getZ() - region.min.getZ() + 1)
				);
				pos = findSurface(world, pos);

				Entity entity = entityType.spawn(world, null, null, null, pos, SpawnReason.SPAWN_EGG, true, false);
				if (entity instanceof MobEntity) {
					((MobEntity) entity).enablePersistence();
					entity.setInvulnerable(true);
				}

				count++;
			}
		}

		return count;
	}

	private BlockPos findSurface(ServerWorld world, BlockPos pos) {
		IChunk chunk = world.getChunk(pos);

		BlockPos.Mutable mutablePos = new BlockPos.Mutable(pos);
		while (!chunk.getBlockState(mutablePos).isAir()) {
			mutablePos.move(Direction.UP);
		}

		return mutablePos.toImmutable().up();
	}

	public static <T> SpawnCreaturesBehavior parse(Dynamic<T> root) {
		List<EntitySpawner> entitySpawners = root.get("creatures").asList(EntitySpawner::parse);
		return new SpawnCreaturesBehavior(entitySpawners);
	}

	private static class EntitySpawner {
		private final String region;
		private final EntityType<?> entity;
		private final int count;

		EntitySpawner(String region, EntityType<?> entity, int count) {
			this.region = region;
			this.entity = entity;
			this.count = count;
		}

		static <T> EntitySpawner parse(Dynamic<T> root) {
			String region = root.get("region").asString("");
			EntityType<?> entityType = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(root.get("entity").asString("")));
			int count = root.get("count").asInt(0);
			return new EntitySpawner(region, entityType, count);
		}
	}
}
