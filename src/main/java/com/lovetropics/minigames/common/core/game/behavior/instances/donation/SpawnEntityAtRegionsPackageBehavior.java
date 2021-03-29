package com.lovetropics.minigames.common.core.game.behavior.instances.donation;

import com.google.common.collect.Lists;
import com.lovetropics.minigames.common.util.Util;
import com.lovetropics.minigames.common.core.integration.game_actions.GamePackage;
import com.lovetropics.minigames.common.core.map.MapRegion;
import com.lovetropics.minigames.common.core.map.MapRegions;
import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.behavior.IGamePackageBehavior;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.Heightmap;

import java.util.List;

public class SpawnEntityAtRegionsPackageBehavior implements IGamePackageBehavior
{
	public static final Codec<SpawnEntityAtRegionsPackageBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				DonationPackageData.CODEC.forGetter(c -> c.data),
				Codec.STRING.listOf().fieldOf("regions_to_spawn_at").forGetter(c -> c.regionsToSpawnAtKeys),
				Registry.ENTITY_TYPE.fieldOf("entity_id").forGetter(c -> c.entityId),
				Codec.INT.optionalFieldOf("entity_count_per_region", 1).forGetter(c -> c.entityCountPerRegion)
		).apply(instance, SpawnEntityAtRegionsPackageBehavior::new);
	});

	private final DonationPackageData data;
	private final List<String> regionsToSpawnAtKeys;
	private final EntityType<?> entityId;
	private final int entityCountPerRegion;

	private final List<MapRegion> regionsToSpawnAt = Lists.newArrayList();

	public SpawnEntityAtRegionsPackageBehavior(final DonationPackageData data, final List<String> regionsToSpawnAtKeys, final EntityType<?> entityId, final int entityCountPerRegion) {
		this.data = data;
		this.regionsToSpawnAtKeys = regionsToSpawnAtKeys;
		this.entityId = entityId;
		this.entityCountPerRegion = entityCountPerRegion;
	}

	@Override
	public String getPackageType() {
		return data.getPackageType();
	}

	@Override
	public void onConstruct(IGameInstance minigame) {
		MapRegions regions = minigame.getMapRegions();

		regionsToSpawnAt.clear();

		for (String key : regionsToSpawnAtKeys) {
			regionsToSpawnAt.addAll(regions.get(key));
		}
	}

	@Override
	public boolean onGamePackageReceived(final IGameInstance minigame, final GamePackage gamePackage) {
		if (gamePackage.getPackageType().equals(data.packageType)) {
			for (final MapRegion region : regionsToSpawnAt) {
				for (int i = 0; i < entityCountPerRegion; i++) {
					final BlockPos pos = minigame.getWorld().getHeight(Heightmap.Type.WORLD_SURFACE, region.sample(minigame.getWorld().getRandom()));

					Util.spawnEntity(entityId, minigame.getWorld(), pos.getX(), pos.getY(), pos.getZ());
				}
			}

			data.onReceive(minigame, null, gamePackage.getSendingPlayerName());

			return true;
		}

		return false;
	}
}
