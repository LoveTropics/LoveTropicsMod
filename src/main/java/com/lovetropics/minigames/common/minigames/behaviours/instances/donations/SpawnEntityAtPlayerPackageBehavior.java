package com.lovetropics.minigames.common.minigames.behaviours.instances.donations;

import com.lovetropics.minigames.common.Util;
import com.mojang.datafixers.Dynamic;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;

public class SpawnEntityAtPlayerPackageBehavior extends DonationPackageBehavior
{
	private final ResourceLocation entityId;
	private final int damagePlayerAmount;

	public SpawnEntityAtPlayerPackageBehavior(final DonationPackageData data, final ResourceLocation entityId, final int damagePlayerAmount) {
		super(data);

		this.entityId = entityId;
		this.damagePlayerAmount = damagePlayerAmount;
	}

	public static <T> SpawnEntityAtPlayerPackageBehavior parse(Dynamic<T> root) {
		final DonationPackageData data = DonationPackageData.parse(root);
		final ResourceLocation entity_id = new ResourceLocation(root.get("entity_id").asString(""));
		final int damagePlayerAmount = root.get("damage_player_amount").asInt(0);

		return new SpawnEntityAtPlayerPackageBehavior(data, entity_id, damagePlayerAmount);
	}

	@Override
	protected void receivePackage(final String sendingPlayer, final ServerPlayerEntity player) {
		Util.spawnEntity(entityId, player.getServerWorld(), player.getPosX(), player.getPosY(), player.getPosZ());
		if (damagePlayerAmount > 0) {
			player.attackEntityFrom(DamageSource.GENERIC, damagePlayerAmount);
		}
	}
}
