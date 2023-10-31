package com.lovetropics.minigames.client;

import com.lovetropics.minigames.common.core.diguise.DisguiseType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class MobItemRenderer extends BlockEntityWithoutLevelRenderer {
	private final Minecraft minecraft;
	private final EntityCache entityCache = new EntityCache();
	private final Function<ItemStack, DisguiseType.EntityConfig> entityGetter;
	private final ResourceLocation inventorySprite;

	public MobItemRenderer(final BlockEntityRenderDispatcher dispatcher, final EntityModelSet modelSet, Minecraft minecraft, final Function<ItemStack, DisguiseType.EntityConfig> entityGetter, final ResourceLocation inventorySprite) {
		super(dispatcher, modelSet);
		this.minecraft = minecraft;
		this.entityGetter = entityGetter;
		this.inventorySprite = inventorySprite;
	}

	private static void addVertex(final VertexConsumer consumer, final PoseStack.Pose pose, final float x, final float y, final float u, final float v, final int packedLight, int packedOverlay) {
		final Matrix4f matrix = pose.pose();
		final Matrix3f normal = pose.normal();
		consumer.vertex(matrix, x, y, 100.0f)
				.color(1.0f, 1.0f, 1.0f, 1.0f)
				.uv(u, v)
				.overlayCoords(packedOverlay)
				.uv2(packedLight)
				.normal(normal, 0.0f, 1.0f, 0.0f)
				.endVertex();
	}

	private void drawInventorySprite(final PoseStack poseStack, final MultiBufferSource bufferSource, final int packedLight, final int packedOverlay) {
		final ResourceLocation atlas = TextureAtlas.LOCATION_BLOCKS;
		final TextureAtlasSprite sprite = minecraft.getTextureAtlas(atlas).apply(inventorySprite);
		final VertexConsumer consumer = bufferSource.getBuffer(RenderType.textSeeThrough(atlas));
		final PoseStack.Pose pose = poseStack.last();
		addVertex(consumer, pose, 0.0f, 0.0f, sprite.getU0(), sprite.getV1(), packedLight, packedOverlay);
		addVertex(consumer, pose, 1.0f, 0.0f, sprite.getU1(), sprite.getV1(), packedLight, packedOverlay);
		addVertex(consumer, pose, 1.0f, 1.0f, sprite.getU1(), sprite.getV0(), packedLight, packedOverlay);
		addVertex(consumer, pose, 0.0f, 1.0f, sprite.getU0(), sprite.getV0(), packedLight, packedOverlay);
	}

	@Override
	public void renderByItem(final ItemStack stack, final ItemDisplayContext context, final PoseStack poseStack, final MultiBufferSource bufferSource, final int packedLight, final int packedOverlay) {
		drawEntity(stack, context, poseStack, bufferSource, packedLight);
		if (context == ItemDisplayContext.GUI) {
			drawInventorySprite(poseStack, bufferSource, packedLight, packedOverlay);
		}
	}

	private void drawEntity(ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
		final Minecraft minecraft = Minecraft.getInstance();
		final ClientLevel level = minecraft.level;

		final DisguiseType.EntityConfig entityType = entityGetter.apply(stack);
		if (level == null || entityType == null) {
			return;
		}

		final Entity entity = entityCache.get(level, entityType);
		if (entity == null) {
			return;
		}

		poseStack.pushPose();
		poseStack.translate(0.5f, 0.5f, 0.5f);
		applyTransforms(context, poseStack, entity);

		minecraft.getEntityRenderDispatcher().render(entity, 0.0, 0.0, 0.0, 0.0f, 0.0f, poseStack, bufferSource, packedLight);

		poseStack.popPose();
	}

	private static void applyTransforms(final ItemDisplayContext context, final PoseStack poseStack, final Entity entity) {
		final float scale = getScale(context, entity);
		poseStack.scale(scale, scale, scale);

		final boolean left = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
		switch (context) {
			case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> {
				poseStack.mulPose(Axis.YP.rotationDegrees(left ? 25.0f : -25.0f));
				poseStack.translate(0.0f, -0.2f / scale, 0.0f);
			}
			case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> {
				poseStack.mulPose(Axis.YP.rotationDegrees(left ? 45.0f : -45.0f));
				poseStack.translate(0.0f, -0.1f / scale, -entity.getBbWidth() / 2.0f);
			}
			case HEAD -> {
				poseStack.translate(0.0f, 0.375f / scale, 0.0f);
				poseStack.mulPose(Axis.YP.rotation(Mth.PI));
			}
			case GUI -> {
				poseStack.translate(0.0f, -entity.getBbHeight() / 2.0f, 0.0f);
				poseStack.mulPose(Axis.XP.rotationDegrees(25.0f));
				poseStack.mulPose(Axis.YP.rotationDegrees(315.0f));
			}
			case GROUND -> poseStack.translate(0.0f, -0.2f / scale, 0.0f);
			case FIXED -> {
				poseStack.translate(0.0f, -0.2f / scale - entity.getBbHeight() / 2.0f, 0.0f);
				poseStack.mulPose(Axis.YP.rotation(Mth.PI));
			}
		}
	}

	private static float getScale(final ItemDisplayContext context, final Entity entity) {
		// Approximate size of the entity - overestimate width a bit because bounding boxes are usually too small
		final float entitySize = Math.max(entity.getBbWidth() * 2.0f, entity.getBbHeight());
		final float targetSize = switch (context) {
			case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND, FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> 0.8f;
			case HEAD -> 1.5f;
			case GUI, FIXED -> 0.9f;
			case GROUND -> 0.5f;
			default -> 1.0f;
		};
		return targetSize / Math.max(entitySize, 1.0f);
	}

	private static class EntityCache {
		@Nullable
		private WeakReference<ClientLevel> level;
		private final Map<DisguiseType.EntityConfig, Optional<Entity>> entities = new Object2ObjectOpenHashMap<>();

		@Nullable
		public Entity get(final ClientLevel level, final DisguiseType.EntityConfig type) {
			if (this.level == null || this.level.get() != level) {
				entities.clear();
				this.level = new WeakReference<>(level);
			}
			Optional<Entity> entity = entities.get(type);
			if (entity == null) {
				entity = Optional.ofNullable(type.createEntity(level));
				entities.put(type, entity);
			}
			return entity.orElse(null);
		}
	}
}
