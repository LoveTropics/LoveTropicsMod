package com.lovetropics.minigames.client.game.handler;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.client.game.ClientGameStateManager;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateTypes;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

@EventBusSubscriber(modid = LoveTropics.ID, value = Dist.CLIENT)
public final class HealthTagRenderer  {
	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static final ResourceLocation HEART_CONTAINER_SPRITE = ResourceLocation.withDefaultNamespace("hud/heart/container");
	private static final ResourceLocation HEART_SPRITE = ResourceLocation.withDefaultNamespace("hud/heart/full");

	@SubscribeEvent
	public static void onRenderPlayerName(RenderPlayerEvent.Post event) {
		Player player = event.getEntity();
		if (!player.isCreative() && !player.isSpectator()) {
			if (player == CLIENT.cameraEntity || !Minecraft.renderNames()) {
				return;
			}

			double distanceSq = CLIENT.getEntityRenderDispatcher().distanceToSqr(player);
			if (!ClientHooks.isNameplateInRenderDistance(player, distanceSq) || player.isDiscrete()) {
				return;
			}

			if (ClientGameStateManager.getOrNull(GameClientStateTypes.HEALTH_TAG) != null) {
				renderHealthTag(event.getPoseStack(), player);
			}
		}
	}

	private static void renderHealthTag(PoseStack poseStack, Player player) {
		String healthText = (int) (player.getHealth() / player.getMaxHealth() * 100.0f) + "%";

		Font font = CLIENT.font;

		final float iconSize = 8.0F;
		final float textScale = 1.0F / 2.5F;
		float left = -(font.width(healthText) * textScale + iconSize) / 2.0F;

		poseStack.pushPose();
		poseStack.translate(0.0, player.getBbHeight() + 0.75, 0.0);
		poseStack.mulPose(CLIENT.getEntityRenderDispatcher().cameraOrientation());
		poseStack.scale(-textScale / 16.0f, -textScale / 16.0f, textScale / 16.0f);

		final GuiGraphics graphics = new GuiGraphics(CLIENT, CLIENT.renderBuffers().bufferSource());
		graphics.pose().mulPose(poseStack.last().pose());

		float textX = (left + iconSize) * textScale;
		float textY = -font.lineHeight / 2.0F;
		graphics.drawString(font, healthText, textX, textY, 0xFFFFFFFF, false);

		RenderSystem.enableDepthTest();

		graphics.pose().pushPose();
		graphics.pose().translate(left - 4.5f, -4.5F, 0.0F);
		graphics.blitSprite(HEART_CONTAINER_SPRITE, 0, 0, 9, 9);
		graphics.blitSprite(HEART_SPRITE, 0, 0, 9, 9);
		graphics.pose().popPose();

		poseStack.popPose();

		graphics.flush();
	}
}
