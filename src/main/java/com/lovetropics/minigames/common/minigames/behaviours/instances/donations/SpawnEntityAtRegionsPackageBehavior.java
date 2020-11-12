package com.lovetropics.minigames.common.minigames.behaviours.instances.donations;

import com.google.common.collect.Lists;
import com.lovetropics.minigames.common.Util;
import com.lovetropics.minigames.common.game_actions.DonationPackageGameAction;
import com.lovetropics.minigames.common.map.MapRegion;
import com.lovetropics.minigames.common.map.MapRegions;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.mojang.datafixers.Dynamic;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.gen.Heightmap;

import java.util.List;

public class SpawnEntityAtRegionsPackageBehavior implements IMinigameBehavior
{
	private final String packageType;
	private final ITextComponent messageForPlayer;
	private final String[] regionsToSpawnAtKeys;
	private final ResourceLocation entityId;
	private final int entityCountPerRegion;

	private final List<MapRegion> regionsToSpawnAt = Lists.newArrayList();

	public SpawnEntityAtRegionsPackageBehavior(final String packageType, final ITextComponent messageForPlayer, final String[] regionsToSpawnAtKeys, final ResourceLocation entityId, final int entityCountPerRegion) {
		this.packageType = packageType;
		this.messageForPlayer = messageForPlayer;
		this.regionsToSpawnAtKeys = regionsToSpawnAtKeys;
		this.entityId = entityId;
		this.entityCountPerRegion = entityCountPerRegion;
	}

	@Override
	public void onConstruct(IMinigameInstance minigame) {
		MapRegions regions = minigame.getMapRegions();

		regionsToSpawnAt.clear();

		for (String key : regionsToSpawnAtKeys) {
			regionsToSpawnAt.addAll(regions.get(key));
		}
	}

	public static <T> SpawnEntityAtRegionsPackageBehavior parse(Dynamic<T> root) {
		final String packageType = root.get("package_type").asString("");
		final ITextComponent messageForPlayer = Util.getTextOrNull(root, "message_for_player");
		final String[] regionsToSpawnAt = root.get("regions_to_spawn_at").asList(d -> d.asString("")).toArray(new String[0]);
		final ResourceLocation entityId = new ResourceLocation(root.get("entity_id").asString(""));
		final int entityCountPerRegion = root.get("entity_count_per_region").asInt(1);

		return new SpawnEntityAtRegionsPackageBehavior(packageType, messageForPlayer, regionsToSpawnAt, entityId, entityCountPerRegion);
	}

	@Override
	public boolean onDonationPackageRequested(final IMinigameInstance minigame, final DonationPackageGameAction action) {
		if (action.getPackageType().equals(packageType)) {
			for (final MapRegion region : regionsToSpawnAt) {
				for (int i = 0; i < entityCountPerRegion; i++) {
					final BlockPos pos = minigame.getWorld().getHeight(Heightmap.Type.WORLD_SURFACE, region.sample(minigame.getWorld().getRandom()));

					Util.spawnEntity(entityId, minigame.getWorld(), pos.getX(), pos.getY(), pos.getZ());
				}
			}

			if (messageForPlayer != null) {
				minigame.getParticipants().sendMessage(messageForPlayer);
			}

			return true;
		}

		return false;
	}
}
