package com.lovetropics.minigames.common.core.game.behavior.instances.donation;

import com.lovetropics.minigames.common.util.Util;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.registry.Registry;

public class SpawnEntityAtPlayerPackageBehavior extends DonationPackageBehavior
{
	public static final Codec<SpawnEntityAtPlayerPackageBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				DonationPackageData.CODEC.forGetter(c -> c.data),
				Registry.ENTITY_TYPE.fieldOf("entity_id").forGetter(c -> c.entityId),
				Codec.INT.optionalFieldOf("damage_player_amount", 0).forGetter(c -> c.damagePlayerAmount)
		).apply(instance, SpawnEntityAtPlayerPackageBehavior::new);
	});

	private final EntityType<?> entityId;
	private final int damagePlayerAmount;

	public SpawnEntityAtPlayerPackageBehavior(final DonationPackageData data, final EntityType<?> entityId, final int damagePlayerAmount) {
		super(data);

		this.entityId = entityId;
		this.damagePlayerAmount = damagePlayerAmount;
	}

	@Override
	protected void receivePackage(final String sendingPlayer, final ServerPlayerEntity player) {
		Util.spawnEntity(entityId, player.getServerWorld(), player.getPosX(), player.getPosY(), player.getPosZ());
		if (damagePlayerAmount > 0) {
			player.attackEntityFrom(DamageSource.GENERIC, damagePlayerAmount);
		}
	}
}
