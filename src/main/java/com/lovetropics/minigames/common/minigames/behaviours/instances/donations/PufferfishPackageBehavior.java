package com.lovetropics.minigames.common.minigames.behaviours.instances.donations;

import com.lovetropics.minigames.common.Util;
import com.mojang.datafixers.Dynamic;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;

// TODO: remove when we can combine package behaviors
public final class PufferfishPackageBehavior extends DonationPackageBehavior {
	public PufferfishPackageBehavior(final String packageType, final ITextComponent messageForPlayer, final PlayerSelect playerSelect) {
		super(packageType, messageForPlayer, playerSelect);
	}

	public static <T> PufferfishPackageBehavior parse(Dynamic<T> root) {
		final String packageType = root.get("package_type").asString("");
		final ITextComponent messageForPlayer = Util.getTextOrNull(root, "message_for_player");
		final PlayerSelect playerSelect = PlayerSelect.getFromType(root.get("player_select").asString(PlayerSelect.RANDOM.getType())).get();

		return new PufferfishPackageBehavior(packageType, messageForPlayer, playerSelect);
	}

	@Override
	protected void receivePackage(final String sendingPlayer, final ServerPlayerEntity player) {
		player.world.setBlockState(player.getPosition(), Blocks.WATER.getDefaultState());
		Util.spawnEntity(EntityType.PUFFERFISH, player.getServerWorld(), player.getPosX(), player.getPosY(), player.getPosZ());
	}
}
