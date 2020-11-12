package com.lovetropics.minigames.common.minigames.behaviours.instances.donations;

import com.lovetropics.minigames.common.Util;
import com.mojang.datafixers.Dynamic;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;

public class SpawnEntityAtPlayerPackageBehavior extends DonationPackageBehavior
{
	private final ResourceLocation entityId;

	public SpawnEntityAtPlayerPackageBehavior(final DonationPackageData data, final ResourceLocation entityId) {
		super(data);

		this.entityId = entityId;
	}

	public static <T> SpawnEntityAtPlayerPackageBehavior parse(Dynamic<T> root) {
		final DonationPackageData data = DonationPackageData.parse(root);
		final ResourceLocation entity_id = new ResourceLocation(root.get("entity_id").asString(""));

		return new SpawnEntityAtPlayerPackageBehavior(data, entity_id);
	}

	@Override
	protected void receivePackage(final String sendingPlayer, final ServerPlayerEntity player) {
		Util.spawnEntity(entityId, player.getServerWorld(), player.getPosX(), player.getPosY(), player.getPosZ());
	}
}
