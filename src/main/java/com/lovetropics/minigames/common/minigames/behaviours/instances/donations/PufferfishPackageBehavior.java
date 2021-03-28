package com.lovetropics.minigames.common.minigames.behaviours.instances.donations;

import com.lovetropics.minigames.common.Util;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.ServerPlayerEntity;

// TODO: remove when we can combine package behaviors
public final class PufferfishPackageBehavior extends DonationPackageBehavior {
	public static final Codec<PufferfishPackageBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				DonationPackageData.CODEC.forGetter(c -> c.data)
		).apply(instance, PufferfishPackageBehavior::new);
	});

	public PufferfishPackageBehavior(final DonationPackageData data) {
		super(data);
	}

	@Override
	protected void receivePackage(final String sendingPlayer, final ServerPlayerEntity player) {
		player.world.setBlockState(player.getPosition(), Blocks.WATER.getDefaultState());
		Util.spawnEntity(EntityType.PUFFERFISH, player.getServerWorld(), player.getPosX(), player.getPosY(), player.getPosZ());
	}
}
