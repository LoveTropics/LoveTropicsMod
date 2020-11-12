package com.lovetropics.minigames.common.minigames.behaviours.instances.donations;

import com.lovetropics.minigames.common.Util;
import com.mojang.datafixers.Dynamic;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public final class ForcedPlayerHeadPackageBehavior extends DonationPackageBehavior {
	public ForcedPlayerHeadPackageBehavior(final String packageType, final ITextComponent messageForPlayer, final PlayerSelect playerSelect) {
		super(packageType, messageForPlayer, playerSelect);
	}

	public static <T> ForcedPlayerHeadPackageBehavior parse(Dynamic<T> root) {
		final String packageType = root.get("package_type").asString("");
		final ITextComponent messageForPlayer = Util.getTextOrNull(root, "message_for_player");
		final PlayerSelect playerSelect = PlayerSelect.getFromType(root.get("player_select").asString(PlayerSelect.SPECIFIC.getType())).get();

		return new ForcedPlayerHeadPackageBehavior(packageType, messageForPlayer, playerSelect);
	}

	@Override
	protected void receivePackage(final String sendingPlayer, final ServerPlayerEntity player) {
		ItemStack head = createHeadForSender(sendingPlayer);
		head.addEnchantment(Enchantments.BINDING_CURSE, 1);

		player.setItemStackToSlot(EquipmentSlotType.HEAD, head);
	}

	@Override
	protected boolean shouldGiveSenderHead() {
		return false;
	}
}
