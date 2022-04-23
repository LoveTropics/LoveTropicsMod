package com.lovetropics.minigames.client.render.entity;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.content.survive_the_tide.entity.DriftwoodEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;
import com.mojang.math.Vector3f;

public final class DriftwoodRenderer extends EntityRenderer<DriftwoodEntity> {
	private static final ResourceLocation TEXTURE = new ResourceLocation(Constants.MODID, "textures/entity/driftwood.png");

	private final DriftwoodModel model = new DriftwoodModel();

	public DriftwoodRenderer(EntityRenderDispatcher renderManager) {
		super(renderManager);
	}

	@Override
	public void render(DriftwoodEntity entity, float entityYaw, float partialTicks, PoseStack transform, MultiBufferSource buffer, int light) {
		super.render(entity, entityYaw, partialTicks, transform, buffer, light);

		transform.pushPose();
		transform.translate(0.0, -0.5, 0.0);
		transform.mulPose(Vector3f.YP.rotationDegrees(90.0F - entityYaw));

		VertexConsumer builder = buffer.getBuffer(model.renderType(TEXTURE));
		model.renderToBuffer(transform, builder, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

		transform.popPose();
	}

	@Override
	public ResourceLocation getTextureLocation(DriftwoodEntity entity) {
		return TEXTURE;
	}

	static final class DriftwoodModel extends EntityModel<Entity> {
		private final ModelPart box;

		DriftwoodModel() {
			texWidth = 128;
			texHeight = 32;

			box = new ModelPart(this);
			box.setPos(0.0F, 24.0F, 0.0F);
			box.texOffs(0, 0);
			box.addBox(-16.0F, -16.0F, -8.0F, 32.0F, 16.0F, 16.0F, 0.0F, false);
		}

		@Override
		public void renderToBuffer(PoseStack transform, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
			box.render(transform, buffer, packedLight, packedOverlay);
		}

		@Override
		public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float age, float yaw, float pitch) {
		}
	}
}
