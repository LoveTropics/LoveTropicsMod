package com.lovetropics.minigames.common.core.game.behavior.instances.donation;

import com.google.common.collect.Lists;
import com.lovetropics.lib.BlockBox;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePackageEvents;
import com.lovetropics.minigames.common.core.map.MapRegions;
import com.lovetropics.minigames.common.util.Util;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;

import java.util.List;

public class SpawnEntityAtRegionsPackageBehavior implements IGameBehavior {
	public static final Codec<SpawnEntityAtRegionsPackageBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.STRING.listOf().fieldOf("regions_to_spawn_at").forGetter(c -> c.regionsToSpawnAtKeys),
				Registry.ENTITY_TYPE.fieldOf("entity_id").forGetter(c -> c.entityId),
				Codec.INT.optionalFieldOf("entity_count_per_region", 1).forGetter(c -> c.entityCountPerRegion)
		).apply(instance, SpawnEntityAtRegionsPackageBehavior::new);
	});

	private final List<String> regionsToSpawnAtKeys;
	private final EntityType<?> entityId;
	private final int entityCountPerRegion;

	private final List<BlockBox> regionsToSpawnAt = Lists.newArrayList();

	public SpawnEntityAtRegionsPackageBehavior(final List<String> regionsToSpawnAtKeys, final EntityType<?> entityId, final int entityCountPerRegion) {
		this.regionsToSpawnAtKeys = regionsToSpawnAtKeys;
		this.entityId = entityId;
		this.entityCountPerRegion = entityCountPerRegion;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		MapRegions regions = game.getMapRegions();

		regionsToSpawnAt.clear();
		for (String key : regionsToSpawnAtKeys) {
			regionsToSpawnAt.addAll(regions.get(key));
		}

		events.listen(GamePackageEvents.APPLY_PACKAGE, (player, sendingPlayer) -> {
			ServerWorld world = game.getWorld();
			for (final BlockBox region : regionsToSpawnAt) {
				for (int i = 0; i < entityCountPerRegion; i++) {
					final BlockPos pos = world.getHeight(Heightmap.Type.WORLD_SURFACE, region.sample(world.getRandom()));
					Util.spawnEntity(entityId, world, pos.getX(), pos.getY(), pos.getZ());
				}
			}
		});
	}
}
