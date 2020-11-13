package com.lovetropics.minigames.common.minigames.behaviours.instances.donations;

import com.mojang.datafixers.Dynamic;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;

public final class ForcedPlayerHeadPackageBehavior extends DonationPackageBehavior {
	public ForcedPlayerHeadPackageBehavior(final DonationPackageData data) {
		super(data);
	}

	public static <T> ForcedPlayerHeadPackageBehavior parse(Dynamic<T> root) {
		final DonationPackageData data = DonationPackageData.parse(root);

		return new ForcedPlayerHeadPackageBehavior(data);
	}

	@Override
	protected void receivePackage(final String sendingPlayer, final ServerPlayerEntity player) {
		if (sendingPlayer == null) {
			return;
		}

		ItemStack head = createHeadForSender(sendingPlayer);
		head.addEnchantment(Enchantments.BINDING_CURSE, 1);

		player.setItemStackToSlot(EquipmentSlotType.HEAD, head);
	}

	@Override
	protected boolean shouldGiveSenderHead() {
		return false;
	}
}
