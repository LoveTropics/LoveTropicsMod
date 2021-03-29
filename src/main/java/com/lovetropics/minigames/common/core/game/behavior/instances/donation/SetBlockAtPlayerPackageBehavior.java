package com.lovetropics.minigames.common.core.game.behavior.instances.donation;

import com.lovetropics.minigames.common.util.MoreCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;

public final class SetBlockAtPlayerPackageBehavior extends DonationPackageBehavior {
	public static final Codec<SetBlockAtPlayerPackageBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				DonationPackageData.CODEC.forGetter(c -> c.data),
				MoreCodecs.BLOCK_STATE.fieldOf("block").forGetter(c -> c.block)
		).apply(instance, SetBlockAtPlayerPackageBehavior::new);
	});

	private final BlockState block;

	public SetBlockAtPlayerPackageBehavior(final DonationPackageData data, final BlockState block) {
		super(data);
		this.block = block;
	}
	@Override
	protected void receivePackage(final String sendingPlayer, final ServerPlayerEntity player) {
		player.world.setBlockState(player.getPosition(), block);
	}
}
