package com.lovetropics.minigames.client.game.handler;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.client.game.ClientGameStateManager;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateTypes;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderNameplateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MODID, value = Dist.CLIENT)
public final class HealthTagRenderer  {
	private static final Minecraft CLIENT = Minecraft.getInstance();

	@SubscribeEvent
	public static void onRenderPlayerName(RenderNameplateEvent event) {
		if (event.getEntity() instanceof Player player && !player.isCreative() && !player.isSpectator()) {
			if (player == CLIENT.cameraEntity || !Minecraft.renderNames()) {
				return;
			}

			double distanceSq = CLIENT.getEntityRenderDispatcher().distanceToSqr(player);
			if (!ForgeHooksClient.isNameplateInRenderDistance(player, distanceSq) || player.isDiscrete()) {
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

		float textX = (left + iconSize) * textScale;
		float textY = -font.lineHeight / 2.0F;
		font.draw(poseStack, healthText, textX, textY, 0xFFFFFFFF);

		RenderSystem.enableTexture();
		RenderSystem.enableDepthTest();

		poseStack.pushPose();
		poseStack.translate(left - 4.5f, -4.5F, 0.0F);
		RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		GuiComponent.blit(poseStack, 0, 0, 16, 0, 9, 9, 256, 256);
		GuiComponent.blit(poseStack, 0, 0, 52, 0, 9, 9, 256, 256);
		poseStack.popPose();

		poseStack.popPose();
	}
}
