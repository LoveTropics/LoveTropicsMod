package com.lovetropics.minigames.common.minigames.behaviours.instances.donations;

import com.mojang.datafixers.Dynamic;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;

public final class SetBlockAtPlayerPackageBehavior extends DonationPackageBehavior {
	private final BlockState block;

	public SetBlockAtPlayerPackageBehavior(final DonationPackageData data, final BlockState block) {
		super(data);
		this.block = block;
	}

	public static <T> SetBlockAtPlayerPackageBehavior parse(Dynamic<T> root) {
		final DonationPackageData data = DonationPackageData.parse(root);
		final BlockState block = BlockState.deserialize(root.get("block").orElseEmptyMap());

		return new SetBlockAtPlayerPackageBehavior(data, block);
	}

	@Override
	protected void receivePackage(final String sendingPlayer, final ServerPlayerEntity player) {
		player.world.setBlockState(player.getPosition(), block);
	}
}
