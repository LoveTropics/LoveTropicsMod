package com.lovetropics.minigames.common.core.game.behavior.instances.action;

import com.google.common.collect.Lists;
import com.lovetropics.lib.BlockBox;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameActionEvents;
import com.lovetropics.minigames.common.core.map.MapRegions;
import com.lovetropics.minigames.common.util.Util;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.List;

public class SpawnEntityAtRegionsAction implements IGameBehavior {
	public static final MapCodec<SpawnEntityAtRegionsAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.STRING.listOf().fieldOf("regions_to_spawn_at").forGetter(c -> c.regionsToSpawnAtKeys),
			BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("entity_id").forGetter(c -> c.entityId),
			Codec.INT.optionalFieldOf("entity_count_per_region", 1).forGetter(c -> c.entityCountPerRegion)
	).apply(i, SpawnEntityAtRegionsAction::new));

	private final List<String> regionsToSpawnAtKeys;
	private final EntityType<?> entityId;
	private final int entityCountPerRegion;

	private final List<BlockBox> regionsToSpawnAt = Lists.newArrayList();

	public SpawnEntityAtRegionsAction(final List<String> regionsToSpawnAtKeys, final EntityType<?> entityId, final int entityCountPerRegion) {
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

		events.listen(GameActionEvents.APPLY, (context) -> {
			if (regionsToSpawnAt.isEmpty()) {
				return false;
			}

			ServerLevel world = game.getWorld();
			for (final BlockBox region : regionsToSpawnAt) {
				for (int i = 0; i < entityCountPerRegion; i++) {
					final BlockPos pos = world.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, region.sample(world.getRandom()));
					Util.spawnEntity(entityId, world, pos.getX(), pos.getY(), pos.getZ());
				}
			}

			return true;
		});
	}
}
