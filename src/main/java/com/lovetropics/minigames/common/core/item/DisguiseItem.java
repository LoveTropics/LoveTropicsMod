package com.lovetropics.minigames.common.core.item;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.client.CustomItemRenderers;
import com.lovetropics.minigames.common.core.diguise.DisguiseType;
import com.lovetropics.minigames.common.core.diguise.ServerPlayerDisguises;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;

import java.util.function.Consumer;

@EventBusSubscriber(modid = LoveTropics.ID)
public class DisguiseItem extends Item implements Equipable {
	public DisguiseItem(final Properties properties) {
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
		final DisguiseType disguiseType = stack.get(MinigameDataComponents.DISGUISE);
		if (disguiseType != null && disguiseType.entityType() != null) {
			return Component.translatable(getDescriptionId() + ".entity", disguiseType.entityType().getDescription());
		}
		return super.getName(stack);
	}

	@Override
	public void initializeClient(final Consumer<IClientItemExtensions> consumer) {
		consumer.accept(CustomItemRenderers.disguiseItem());
	}

	@SubscribeEvent
	public static void onEquipmentChange(final LivingEquipmentChangeEvent event) {
		if (event.getSlot() == EquipmentSlot.HEAD && event.getEntity() instanceof final ServerPlayer player) {
            final DisguiseType fromDisguise = event.getFrom().get(MinigameDataComponents.DISGUISE);
            final DisguiseType toDisguise = event.getTo().get(MinigameDataComponents.DISGUISE);
			if (fromDisguise != null || toDisguise != null) {
				if (toDisguise != null) {
					ServerPlayerDisguises.set(player, toDisguise);
				} else {
					ServerPlayerDisguises.clear(player);
				}
			}
		}
	}
}
