package com.lovetropics.minigames.client.toast;

import com.lovetropics.minigames.common.util.MoreCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.Effect;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Optional;

public final class NotificationIcon {
	public static final Codec<NotificationIcon> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				MoreCodecs.ITEM_STACK.optionalFieldOf("item").forGetter(c -> Optional.ofNullable(c.item)),
				Registry.EFFECTS.optionalFieldOf("effect").forGetter(c -> Optional.ofNullable(c.effect))
		).apply(instance, NotificationIcon::new);
	});

	public final ItemStack item;
	public final Effect effect;

	private NotificationIcon(Optional<ItemStack> item, Optional<Effect> effect) {
		this(item.orElse(null), effect.orElse(null));
	}

	private NotificationIcon(@Nullable ItemStack item, @Nullable Effect effect) {
		this.item = item;
		this.effect = effect;
	}

	public static NotificationIcon item(ItemStack item) {
		return new NotificationIcon(item, null);
	}

	public static NotificationIcon effect(Effect effect) {
		return new NotificationIcon(null, effect);
	}

	public void encode(PacketBuffer buffer) {
		if (this.item != null) {
			buffer.writeBoolean(true);
			buffer.writeItemStack(this.item);
		} else {
			buffer.writeBoolean(false);
			buffer.writeRegistryIdUnsafe(ForgeRegistries.POTIONS, this.effect);
		}
	}

	public static NotificationIcon decode(PacketBuffer buffer) {
		if (buffer.readBoolean()) {
			return NotificationIcon.item(buffer.readItemStack());
		} else {
			return NotificationIcon.effect(buffer.readRegistryIdUnsafe(ForgeRegistries.POTIONS));
		}
	}
}
