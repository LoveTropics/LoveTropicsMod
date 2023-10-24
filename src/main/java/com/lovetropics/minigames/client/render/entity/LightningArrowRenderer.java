package com.lovetropics.minigames.client.render.entity;

import com.lovetropics.minigames.common.content.survive_the_tide.entity.LightningArrowEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.TippableArrowRenderer;
import net.minecraft.resources.ResourceLocation;

public class LightningArrowRenderer extends ArrowRenderer<LightningArrowEntity> {
	public LightningArrowRenderer(final EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public ResourceLocation getTextureLocation(final LightningArrowEntity entity) {
		return TippableArrowRenderer.NORMAL_ARROW_LOCATION;
	}
}
