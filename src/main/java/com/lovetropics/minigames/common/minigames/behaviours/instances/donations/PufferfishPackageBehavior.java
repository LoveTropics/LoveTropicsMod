package com.lovetropics.minigames.common.minigames.behaviours.instances.donations;

import com.lovetropics.minigames.common.Util;
import com.mojang.datafixers.Dynamic;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.ServerPlayerEntity;

// TODO: remove when we can combine package behaviors
public final class PufferfishPackageBehavior extends DonationPackageBehavior {
	public PufferfishPackageBehavior(final DonationPackageData data) {
		super(data);
	}

	public static <T> PufferfishPackageBehavior parse(Dynamic<T> root) {
		final DonationPackageData data = DonationPackageData.parse(root);

		return new PufferfishPackageBehavior(data);
	}

	@Override
	protected void receivePackage(final String sendingPlayer, final ServerPlayerEntity player) {
		player.world.setBlockState(player.getPosition(), Blocks.WATER.getDefaultState());
		Util.spawnEntity(EntityType.PUFFERFISH, player.getServerWorld(), player.getPosX(), player.getPosY(), player.getPosZ());
	}
}
