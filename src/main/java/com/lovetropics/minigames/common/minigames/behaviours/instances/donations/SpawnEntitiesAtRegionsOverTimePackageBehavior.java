package com.lovetropics.minigames.common.minigames.behaviours.instances.donations;

import com.google.common.collect.Lists;
import com.lovetropics.minigames.common.Util;
import com.lovetropics.minigames.common.game_actions.GamePackage;
import com.lovetropics.minigames.common.map.MapRegion;
import com.lovetropics.minigames.common.map.MapRegions;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.mojang.datafixers.Dynamic;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;

import java.util.List;

/**
 * Spawns an amount of entities over a set amount of ticks, spread randomly across all the given regions
 */

public class SpawnEntitiesAtRegionsOverTimePackageBehavior implements IMinigameBehavior
{
	private final DonationPackageData data;
	private final String[] regionsToSpawnAtKeys;
	private final ResourceLocation entityId;
	private final int entityCount;
	private int ticksToSpawnFor;

	//runtime adjusted vars
	private int ticksRemaining;
	private int entityCountRemaining;

	private final List<MapRegion> regionsToSpawnAt = Lists.newArrayList();

	public SpawnEntitiesAtRegionsOverTimePackageBehavior(final DonationPackageData data, final String[] regionsToSpawnAtKeys, final ResourceLocation entityId, final int entityCount, final int ticksToSpawnFor) {
		this.data = data;
		this.regionsToSpawnAtKeys = regionsToSpawnAtKeys;
		this.entityId = entityId;
		this.entityCount = entityCount;
		this.ticksToSpawnFor = ticksToSpawnFor;
	}

	@Override
	public void onConstruct(IMinigameInstance minigame) {
		MapRegions regions = minigame.getMapRegions();

		regionsToSpawnAt.clear();

		for (String key : regionsToSpawnAtKeys) {
			regionsToSpawnAt.addAll(regions.get(key));
		}
	}

	public static <T> SpawnEntitiesAtRegionsOverTimePackageBehavior parse(Dynamic<T> root) {
		final DonationPackageData data = DonationPackageData.parse(root);
		final String[] regionsToSpawnAt = root.get("regions_to_spawn_at").asList(d -> d.asString("")).toArray(new String[0]);
		final ResourceLocation entityId = new ResourceLocation(root.get("entity_id").asString(""));
		final int entityCount = root.get("entity_count").asInt(1);
		final int ticksToSpawnFor = root.get("ticks_to_spawn_for").asInt(1);

		return new SpawnEntitiesAtRegionsOverTimePackageBehavior(data, regionsToSpawnAt, entityId, entityCount, ticksToSpawnFor);
	}

	@Override
	public boolean onGamePackageReceived(final IMinigameInstance minigame, final GamePackage gamePackage) {
		if (gamePackage.getPackageType().equals(data.packageType)) {

			ticksRemaining += ticksToSpawnFor;
			entityCountRemaining += entityCount;

			minigame.getParticipants().forEach(player -> data.onReceive(player, gamePackage.getSendingPlayerName()));

			return true;
		}

		return false;
	}

	@Override
	public void worldUpdate(IMinigameInstance minigame, World world) {
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
