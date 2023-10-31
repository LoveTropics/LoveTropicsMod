package com.lovetropics.minigames.client;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.core.diguise.DisguiseType;
import com.lovetropics.minigames.common.core.item.DisguiseItem;
import com.lovetropics.minigames.common.core.item.MobHatItem;
import com.lovetropics.minigames.common.core.item.PlushieItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public class CustomItemRenderers {
	private static final ResourceLocation DISGUISE_ITEM_SPRITE = new ResourceLocation(Constants.MODID, "item/disguise");
	private static final ResourceLocation MOB_HAT_SPRITE = new ResourceLocation(Constants.MODID, "item/mob_hat");

	public static IClientItemExtensions disguiseItem() {
		final Minecraft minecraft = Minecraft.getInstance();
		return createExtensions(new MobItemRenderer(minecraft.getBlockEntityRenderDispatcher(), minecraft.getEntityModels(), minecraft, stack -> {
			final DisguiseType disguise = DisguiseItem.getDisguiseType(stack);
			return disguise != null ? disguise.entity() : null;
		}, stack -> 1.0f, DISGUISE_ITEM_SPRITE));
	}

	public static IClientItemExtensions mobHatItem() {
		final Minecraft minecraft = Minecraft.getInstance();
		return createExtensions(new MobItemRenderer(minecraft.getBlockEntityRenderDispatcher(), minecraft.getEntityModels(), minecraft, MobHatItem::getEntityType, stack -> 1.0f, MOB_HAT_SPRITE));
	}

	public static IClientItemExtensions plushieItem() {
		final Minecraft minecraft = Minecraft.getInstance();
		return createExtensions(new MobItemRenderer(minecraft.getBlockEntityRenderDispatcher(), minecraft.getEntityModels(), minecraft, PlushieItem::getEntityType, PlushieItem::getSize, null));
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
