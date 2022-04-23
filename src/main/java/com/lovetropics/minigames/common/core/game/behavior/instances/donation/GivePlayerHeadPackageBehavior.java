package com.lovetropics.minigames.common.core.game.behavior.instances.donation;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePackageEvents;
import com.lovetropics.minigames.common.util.Util;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class GivePlayerHeadPackageBehavior implements IGameBehavior {
	public static final Codec<GivePlayerHeadPackageBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.BOOL.optionalFieldOf("forced", false).forGetter(c -> c.forced)
		).apply(instance, GivePlayerHeadPackageBehavior::new);
	});

	private final boolean forced;

	public GivePlayerHeadPackageBehavior(boolean forced) {
		this.forced = forced;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GamePackageEvents.APPLY_PACKAGE_TO_PLAYER, (player, sendingPlayer) -> {
			if (sendingPlayer == null) {
				sendingPlayer = "LoveTropics";
			}

			ItemStack head = createHeadForSender(sendingPlayer);
			if (forced) {
				head.enchant(Enchantments.BINDING_CURSE, 1);
				player.setItemSlot(EquipmentSlot.HEAD, head);
			} else {
				Util.addItemStackToInventory(player, head);
			}

			return true;
		});
	}

	private ItemStack createHeadForSender(String sendingPlayer) {
		final ItemStack senderHead = new ItemStack(Items.PLAYER_HEAD);
		senderHead.getOrCreateTag().putString("SkullOwner", sendingPlayer);
		return senderHead;
	}
}
