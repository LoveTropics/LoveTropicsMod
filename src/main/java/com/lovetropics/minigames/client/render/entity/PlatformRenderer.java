package com.lovetropics.minigames.client.render.entity;

import com.lovetropics.minigames.common.content.survive_the_tide.entity.PlatformEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CommonColors;
import org.joml.Matrix4f;

public final class PlatformRenderer extends EntityRenderer<PlatformEntity> {
	private final BlockModelShaper blockModelShaper;

	public PlatformRenderer(EntityRendererProvider.Context context) {
		super(context);
		blockModelShaper = context.getBlockRenderDispatcher().getBlockModelShaper();
	}

	@Override
	public void render(PlatformEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light) {
		super.render(entity, entityYaw, partialTicks, poseStack, buffer, light);

		float width = entity.getWidth();
		VertexConsumer builder = buffer.getBuffer(RenderType.entitySolid(getTextureLocation(entity)));
		Matrix4f pose = poseStack.last().pose();
		builder.addVertex(pose, -width / 2.0f, 0.0f, -width / 2.0f).setColor(CommonColors.WHITE).setUv(0.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0.0f, 1.0f, 0.0f);
		builder.addVertex(pose, -width / 2.0f, 0.0f, width / 2.0f).setColor(CommonColors.WHITE).setUv(0.0f, width).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0.0f, 1.0f, 0.0f);
		builder.addVertex(pose, width / 2.0f, 0.0f, width / 2.0f).setColor(CommonColors.WHITE).setUv(width, width).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0.0f, 1.0f, 0.0f);
		builder.addVertex(pose, width / 2.0f, 0.0f, -width / 2.0f).setColor(CommonColors.WHITE).setUv(width, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0.0f, 1.0f, 0.0f);
	}

	@Override
	public ResourceLocation getTextureLocation(PlatformEntity entity) {
		TextureAtlasSprite sprite = blockModelShaper.getTexture(entity.getBlockState(), entity.level(), BlockPos.ZERO);
		// This is an extreme hack, and not guaranteed to be correct
		return sprite.contents().name().withPath(p -> "textures/" + p + ".png");
	}
}
