package com.lovetropics.minigames.client;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.core.diguise.DisguiseType;
import com.lovetropics.minigames.common.core.item.MinigameDataComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class CustomItemRenderers {
	private static final ResourceLocation DISGUISE_ITEM_SPRITE = LoveTropics.location("item/disguise");
	private static final ResourceLocation MOB_HAT_SPRITE = LoveTropics.location("item/mob_hat");

	public static IClientItemExtensions disguiseItem() {
		final Minecraft minecraft = Minecraft.getInstance();
		return createExtensions(new MobItemRenderer(minecraft.getBlockEntityRenderDispatcher(), minecraft.getEntityModels(), minecraft, stack -> {
            final DisguiseType disguise = stack.get(MinigameDataComponents.DISGUISE);
			return disguise != null ? disguise.entity() : null;
		}, stack -> 1.0f, DISGUISE_ITEM_SPRITE));
	}

	public static IClientItemExtensions mobHatItem() {
		final Minecraft minecraft = Minecraft.getInstance();
		return createExtensions(new MobItemRenderer(minecraft.getBlockEntityRenderDispatcher(), minecraft.getEntityModels(), minecraft, stack -> stack.get(MinigameDataComponents.ENTITY), stack -> stack.getOrDefault(MinigameDataComponents.SIZE, 1.0f), MOB_HAT_SPRITE));
	}

	public static IClientItemExtensions plushieItem() {
		final Minecraft minecraft = Minecraft.getInstance();
		return createExtensions(new MobItemRenderer(minecraft.getBlockEntityRenderDispatcher(), minecraft.getEntityModels(), minecraft, stack -> stack.get(MinigameDataComponents.ENTITY), stack1 -> stack1.getOrDefault(MinigameDataComponents.SIZE, 1.0f), null));
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
