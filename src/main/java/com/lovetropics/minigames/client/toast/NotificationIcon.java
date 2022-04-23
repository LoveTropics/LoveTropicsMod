package com.lovetropics.minigames.client.toast;

import com.lovetropics.lib.codec.MoreCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Optional;

public final class NotificationIcon {
	public static final Codec<NotificationIcon> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				MoreCodecs.ITEM_STACK.optionalFieldOf("item").forGetter(c -> Optional.ofNullable(c.item)),
				ForgeRegistries.MOB_EFFECTS.getCodec().optionalFieldOf("effect").forGetter(c -> Optional.ofNullable(c.effect))
		).apply(instance, NotificationIcon::new);
	});

	public final ItemStack item;
	public final MobEffect effect;

	private NotificationIcon(Optional<ItemStack> item, Optional<MobEffect> effect) {
		this(item.orElse(null), effect.orElse(null));
	}

	private NotificationIcon(@Nullable ItemStack item, @Nullable MobEffect effect) {
		this.item = item;
		this.effect = effect;
	}

	public static NotificationIcon item(ItemStack item) {
		return new NotificationIcon(item, null);
	}

	public static NotificationIcon effect(MobEffect effect) {
		return new NotificationIcon(null, effect);
	}

	public void encode(FriendlyByteBuf buffer) {
		if (this.item != null) {
			buffer.writeBoolean(true);
			buffer.writeItem(this.item);
		} else {
			buffer.writeBoolean(false);
			buffer.writeRegistryIdUnsafe(ForgeRegistries.MOB_EFFECTS, this.effect);
		}
	}

	public static NotificationIcon decode(FriendlyByteBuf buffer) {
		if (buffer.readBoolean()) {
			return NotificationIcon.item(buffer.readItem());
		} else {
			return NotificationIcon.effect(buffer.readRegistryIdUnsafe(ForgeRegistries.MOB_EFFECTS));
		}
	}
}
