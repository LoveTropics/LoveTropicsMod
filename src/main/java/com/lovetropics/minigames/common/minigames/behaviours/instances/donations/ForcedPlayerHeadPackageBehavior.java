package com.lovetropics.minigames.common.minigames.behaviours.instances.donations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;

public final class ForcedPlayerHeadPackageBehavior extends DonationPackageBehavior {
	public static final Codec<ForcedPlayerHeadPackageBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				DonationPackageData.CODEC.forGetter(c -> c.data)
		).apply(instance, ForcedPlayerHeadPackageBehavior::new);
	});

	public ForcedPlayerHeadPackageBehavior(final DonationPackageData data) {
		super(data);
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
