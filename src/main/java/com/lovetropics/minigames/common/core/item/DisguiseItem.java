package com.lovetropics.minigames.common.core.item;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.client.CustomItemRenderers;
import com.lovetropics.minigames.common.core.diguise.DisguiseType;
import com.lovetropics.minigames.common.core.diguise.ServerPlayerDisguises;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
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
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = Constants.MODID)
public class DisguiseItem extends Item implements Equipable {
	private static final String KEY_DISGUISE = "disguise";

	public DisguiseItem(final Properties properties) {
		super(properties);
	}

	// I love unpacking NBT every frame, thanks ItemStack.
	@Nullable
	public static DisguiseType getDisguiseType(final ItemStack stack) {
		if (!stack.is(MinigameItems.DISGUISE.get())) {
			return null;
		}
		final CompoundTag tag = stack.getTag();
		if (tag == null || !tag.contains(KEY_DISGUISE)) {
			return null;
		}
		return DisguiseType.CODEC.parse(NbtOps.INSTANCE, tag.get(KEY_DISGUISE)).result().orElse(null);
	}

	public static void setDisguiseType(final ItemStack stack, final DisguiseType disguiseType) {
		stack.getOrCreateTag().put(KEY_DISGUISE, Util.getOrThrow(DisguiseType.CODEC.encodeStart(NbtOps.INSTANCE, disguiseType), IllegalStateException::new));
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
		final DisguiseType disguiseType = getDisguiseType(stack);
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
			final DisguiseType fromDisguise = getDisguiseType(event.getFrom());
			final DisguiseType toDisguise = getDisguiseType(event.getTo());
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
