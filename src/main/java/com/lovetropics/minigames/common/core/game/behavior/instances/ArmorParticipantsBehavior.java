package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public record ArmorParticipantsBehavior(ItemStack head, ItemStack chest, ItemStack legs, ItemStack feet) implements IGameBehavior {
	public static final MapCodec<ArmorParticipantsBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			MoreCodecs.ITEM_STACK.optionalFieldOf("head", ItemStack.EMPTY).forGetter(c -> c.head),
			MoreCodecs.ITEM_STACK.optionalFieldOf("chest", ItemStack.EMPTY).forGetter(c -> c.chest),
			MoreCodecs.ITEM_STACK.optionalFieldOf("legs", ItemStack.EMPTY).forGetter(c -> c.legs),
			MoreCodecs.ITEM_STACK.optionalFieldOf("feet", ItemStack.EMPTY).forGetter(c -> c.feet)
	).apply(i, ArmorParticipantsBehavior::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GamePlayerEvents.SET_ROLE, (player, role, lastRole) -> {
			if (role == PlayerRole.PARTICIPANT) {
				player.setItemSlot(EquipmentSlot.HEAD, head);
				player.setItemSlot(EquipmentSlot.CHEST, chest);
				player.setItemSlot(EquipmentSlot.LEGS, legs);
				player.setItemSlot(EquipmentSlot.FEET, feet);
			}
		});
	}
}
