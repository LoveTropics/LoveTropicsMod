package com.lovetropics.minigames.common.core.item;

import com.lovetropics.minigames.client.CustomItemRenderers;
import com.lovetropics.minigames.common.core.diguise.DisguiseType;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class MobHatItem extends Item implements Equipable {
	private static final String KEY_ENTITY = "entity";

	public MobHatItem(final Properties properties) {
		super(properties);
	}

	// I love unpacking NBT every frame, thanks ItemStack.
	@Nullable
	public static DisguiseType.EntityConfig getEntityType(final ItemStack stack) {
		if (!stack.is(MinigameItems.MOB_HAT.get())) {
			return null;
		}
		final CompoundTag tag = stack.getTag();
		if (tag == null || !tag.contains(KEY_ENTITY)) {
			return null;
		}
		return DisguiseType.EntityConfig.CODEC.parse(NbtOps.INSTANCE, tag.get(KEY_ENTITY)).result().orElse(null);
	}

	public static void setEntityType(final ItemStack stack, final DisguiseType.EntityConfig entityType) {
		stack.getOrCreateTag().put(KEY_ENTITY, Util.getOrThrow(DisguiseType.EntityConfig.CODEC.encodeStart(NbtOps.INSTANCE, entityType), IllegalStateException::new));
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
		final DisguiseType.EntityConfig entityType = getEntityType(stack);
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
