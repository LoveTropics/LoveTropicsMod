package com.lovetropics.minigames.common.core.item;

import com.lovetropics.minigames.client.CustomItemRenderers;
import com.lovetropics.minigames.common.core.diguise.DisguiseType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class PlushieItem extends Item {
	private static final String KEY_ENTITY = "entity";
	private static final String KEY_SIZE = "size";

	public PlushieItem(final Properties properties) {
		super(properties);
	}

	// I love unpacking NBT every frame, thanks ItemStack.
	@Nullable
	public static DisguiseType.EntityConfig getEntityType(final ItemStack stack) {
		if (!stack.is(MinigameItems.PLUSHIE.get())) {
			return null;
		}
		final CompoundTag tag = stack.getTag();
		if (tag == null || !tag.contains(KEY_ENTITY)) {
			return null;
		}
		return DisguiseType.EntityConfig.CODEC.parse(NbtOps.INSTANCE, tag.get(KEY_ENTITY)).result().orElse(null);
	}

	public static float getSize(final ItemStack stack) {
		if (!stack.is(MinigameItems.PLUSHIE.get())) {
			return 1.0f;
		}
		final CompoundTag tag = stack.getTag();
		if (tag != null && tag.contains(KEY_SIZE, Tag.TAG_FLOAT)) {
			return tag.getFloat(KEY_SIZE);
		}
		return 1.0f;
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
		consumer.accept(CustomItemRenderers.plushieItem());
	}
}
