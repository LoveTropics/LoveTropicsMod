package com.lovetropics.minigames.client.render.entity;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.content.survive_the_tide.entity.DriftwoodEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.HierarchicalModel;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public final class DriftwoodRenderer extends EntityRenderer<DriftwoodEntity> {
	private static final ResourceLocation TEXTURE = LoveTropics.location("textures/entity/driftwood.png");

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
		model.renderToBuffer(poseStack, builder, light, OverlayTexture.NO_OVERLAY);

		poseStack.popPose();
	}

	@Override
	public ResourceLocation getTextureLocation(DriftwoodEntity entity) {
		return TEXTURE;
	}

	@EventBusSubscriber(modid = LoveTropics.ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
	public static final class DriftwoodModel extends HierarchicalModel<Entity> {
		public static final ModelLayerLocation LAYER = new ModelLayerLocation(LoveTropics.location("driftwood"), "main");

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
		public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float age, float yaw, float pitch) {
		}

		@Override
		public ModelPart root() {
			return log;
		}
	}
}
