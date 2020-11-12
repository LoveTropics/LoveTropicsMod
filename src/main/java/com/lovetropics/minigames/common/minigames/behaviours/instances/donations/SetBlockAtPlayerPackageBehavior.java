package com.lovetropics.minigames.common.minigames.behaviours.instances.donations;

import com.lovetropics.minigames.common.Util;
import com.mojang.datafixers.Dynamic;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;

public final class SetBlockAtPlayerPackageBehavior extends DonationPackageBehavior {
	private final BlockState block;

	public SetBlockAtPlayerPackageBehavior(final String packageType, final BlockState block, final ITextComponent messageForPlayer, final PlayerSelect playerSelect) {
		super(packageType, messageForPlayer, playerSelect);
		this.block = block;
	}

	public static <T> SetBlockAtPlayerPackageBehavior parse(Dynamic<T> root) {
		final String packageType = root.get("package_type").asString("");
		final BlockState block = BlockState.deserialize(root.get("block").orElseEmptyMap());
		final ITextComponent messageForPlayer = Util.getText(root, "message_for_player");
		final PlayerSelect playerSelect = PlayerSelect.getFromType(root.get("player_select").asString(PlayerSelect.RANDOM.getType())).get();

		return new SetBlockAtPlayerPackageBehavior(packageType, block, messageForPlayer, playerSelect);
	}

	@Override
	protected void receivePackage(final String sendingPlayer, final ServerPlayerEntity player) {
		player.world.setBlockState(player.getPosition(), block);
	}
}
