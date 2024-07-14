package com.lovetropics.minigames.common.core.item;

import com.lovetropics.minigames.client.CustomItemRenderers;
import com.lovetropics.minigames.common.core.diguise.DisguiseType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class MobHatItem extends Item implements Equipable {
	public MobHatItem(final Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
		return swapWithEquipmentSlot(this, level, player, hand);
	}

	@Override
	public EquipmentSlot getEquipmentSlot() {
		return EquipmentSlot.HEAD;
	}

	@Override
	public Component getName(final ItemStack stack) {
		final DisguiseType.EntityConfig entityType = stack.get(MinigameDataComponents.ENTITY);
		if (entityType != null) {
			return Component.translatable(getDescriptionId() + ".entity", entityType.type().getDescription());
		}
		return super.getName(stack);
	}

	@Override
	public void initializeClient(final Consumer<IClientItemExtensions> consumer) {
		consumer.accept(CustomItemRenderers.mobHatItem());
	}
}
