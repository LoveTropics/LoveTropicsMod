package com.lovetropics.minigames.client;

import com.lovetropics.minigames.common.core.diguise.DisguiseType;
import com.lovetropics.minigames.common.core.item.DisguiseItem;
import com.lovetropics.minigames.common.core.item.MobHatItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public class CustomItemRenderers {
	public static IClientItemExtensions disguiseItem() {
		final Minecraft minecraft = Minecraft.getInstance();
		return createExtensions(new MobItemRenderer(minecraft.getBlockEntityRenderDispatcher(), minecraft.getEntityModels(), stack -> {
			final DisguiseType disguise = DisguiseItem.getDisguiseType(stack);
			return disguise != null ? disguise.entity() : null;
		}));
	}

	public static IClientItemExtensions mobHatItem() {
		final Minecraft minecraft = Minecraft.getInstance();
		return createExtensions(new MobItemRenderer(minecraft.getBlockEntityRenderDispatcher(), minecraft.getEntityModels(), MobHatItem::getEntityType));
	}

	private static IClientItemExtensions createExtensions(final BlockEntityWithoutLevelRenderer renderer) {
		return new IClientItemExtensions() {
			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return renderer;
			}
		};
	}
}
