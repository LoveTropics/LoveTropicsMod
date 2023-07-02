package com.lovetropics.minigames.client.render.entity;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.content.survive_the_tide.entity.DriftwoodEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public final class DriftwoodRenderer extends EntityRenderer<DriftwoodEntity> {
	private static final ResourceLocation TEXTURE = new ResourceLocation(Constants.MODID, "textures/entity/driftwood.png");

	private final DriftwoodModel model;

	public DriftwoodRenderer(EntityRendererProvider.Context context) {
		super(context);
		model = new DriftwoodModel(context.bakeLayer(DriftwoodModel.LAYER));
	}

	@Override
	public void render(DriftwoodEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light) {
		super.render(entity, entityYaw, partialTicks, poseStack, buffer, light);

		poseStack.pushPose();
		poseStack.translate(0.0, -0.5, 0.0);
		poseStack.mulPose(Axis.YP.rotationDegrees(90.0F - entityYaw));

		VertexConsumer builder = buffer.getBuffer(model.renderType(TEXTURE));
		model.renderToBuffer(poseStack, builder, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

		poseStack.popPose();
	}

	@Override
	public ResourceLocation getTextureLocation(DriftwoodEntity entity) {
		return TEXTURE;
	}

	@Mod.EventBusSubscriber(modid = Constants.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
	public static final class DriftwoodModel extends EntityModel<Entity> {
		public static final ModelLayerLocation LAYER = new ModelLayerLocation(new ResourceLocation(Constants.MODID, "driftwood"), "main");

		private final ModelPart log;

		public DriftwoodModel(ModelPart root) {
			log = root.getChild("log");
		}

		@SubscribeEvent
		public static void onRegisterLayerDefinitions(final EntityRenderersEvent.RegisterLayerDefinitions event) {
			event.registerLayerDefinition(LAYER, DriftwoodModel::createBodyModel);
		}

		private static LayerDefinition createBodyModel() {
			final MeshDefinition mesh = new MeshDefinition();
			final PartDefinition root = mesh.getRoot();
			root.addOrReplaceChild("log",
					CubeListBuilder.create()
							.texOffs(0, 0)
							.addBox(-16.0F, -16.0F, -8.0F, 32.0F, 16.0F, 16.0F),
					PartPose.offset(0.0F, 24.0F, 0.0F)
			);
			return LayerDefinition.create(mesh, 128, 32);
		}

		@Override
		public void renderToBuffer(PoseStack transform, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
			log.render(transform, buffer, packedLight, packedOverlay);
		}

		@Override
		public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float age, float yaw, float pitch) {
		}
	}
}
