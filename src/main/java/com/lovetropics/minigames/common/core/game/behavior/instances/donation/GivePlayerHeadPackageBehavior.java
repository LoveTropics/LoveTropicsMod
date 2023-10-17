package com.lovetropics.minigames.common.core.game.behavior.instances.donation;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionParameter;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameActionEvents;
import com.lovetropics.minigames.common.util.Util;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;

public record GivePlayerHeadPackageBehavior(boolean forced) implements IGameBehavior {
	public static final MapCodec<GivePlayerHeadPackageBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.BOOL.optionalFieldOf("forced", false).forGetter(c -> c.forced)
	).apply(i, GivePlayerHeadPackageBehavior::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GameActionEvents.APPLY_TO_PLAYER, (context, player) -> {
			String sendingPlayer = context.get(GameActionParameter.PACKAGE_SENDER).orElse("LoveTropics");

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
