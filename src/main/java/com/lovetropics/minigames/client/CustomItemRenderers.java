package com.lovetropics.minigames.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public class CustomItemRenderers {
	public static IClientItemExtensions disguiseItem() {
		final Minecraft minecraft = Minecraft.getInstance();
		return createExtensions(new DisguiseItemRenderer(minecraft.getBlockEntityRenderDispatcher(), minecraft.getEntityModels()));
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
