package com.lovetropics.minigames.common.content.biodiversity_blitz.client_state.render;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.client.game.ClientGameStateManager;
import com.lovetropics.minigames.common.content.biodiversity_blitz.BiodiversityBlitz;
import com.lovetropics.minigames.common.content.biodiversity_blitz.client_state.ClientBbGlobalState;
import com.lovetropics.minigames.common.content.biodiversity_blitz.client_state.ClientBbSelfState;
import com.lovetropics.minigames.common.content.biodiversity_blitz.client_state.CurrencyTargetState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.CommonColors;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = Constants.MODID, value = Dist.CLIENT)
public final class BbClientRenderEffects {
	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static final int PADDING = 2;

	private static final int ITEM_SIZE = 16;

	public static void registerOverlays(RegisterGuiOverlaysEvent event) {
		event.registerBelow(VanillaGuiOverlay.DEBUG_TEXT.id(), "biodiversity_blitz", (gui, graphics, partialTick, screenWidth, screenHeight) -> {
			ClientBbSelfState selfState = ClientGameStateManager.getOrNull(BiodiversityBlitz.SELF_STATE);
			if (selfState != null) {
				CurrencyTargetState currencyTarget = ClientGameStateManager.getOrNull(BiodiversityBlitz.CURRENCY_TARGET);
				renderOverlay(graphics, selfState, currencyTarget);
			}
		});
	}

	private static void renderOverlay(GuiGraphics graphics, ClientBbSelfState selfState, CurrencyTargetState currencyTarget) {
		Font font = CLIENT.font;

		final int left = PADDING;
		final int top = PADDING;

		int x = left;
		int y = top;

		graphics.renderItem(ClientGameStateManager.getOrNull(BiodiversityBlitz.CURRENCY_ITEM).item(), x, y);

		String currency = String.valueOf(selfState.currency());
		if (currencyTarget != null) {
			currency = ChatFormatting.GRAY + "Total: " + ChatFormatting.WHITE + currency + ChatFormatting.GRAY + "/" + currencyTarget.value();
		}

		graphics.drawString(
				font, currency,
				x + ITEM_SIZE + PADDING,
				y + (ITEM_SIZE - font.lineHeight) / 2,
				CommonColors.WHITE
		);
		y += ITEM_SIZE + PADDING;

		int increment = selfState.nextIncrement();
		boolean gainingCurrency = increment > 0;
		ChatFormatting incrementColor = gainingCurrency ? ChatFormatting.AQUA : ChatFormatting.RED;

		String nextCurrencyIncrement = incrementColor + "+" + increment + ChatFormatting.GRAY + " next drop";
		graphics.drawString(font, nextCurrencyIncrement, x, y, CommonColors.WHITE);
		y += font.lineHeight;

		if (!gainingCurrency) {
			graphics.drawString(font, ChatFormatting.GRAY + "You must be in your plot to receive points!", x, y, CommonColors.WHITE);
		}
	}

	@SubscribeEvent
	public static void onRenderPlayerName(RenderNameTagEvent event) {
		Entity entity = event.getEntity();
		if (entity instanceof Player) {
			ClientBbGlobalState globalState = ClientGameStateManager.getOrNull(BiodiversityBlitz.GLOBAL_STATE);
			if (globalState != null && globalState.hasCurrencyFor(entity.getUUID())) {
				int currency = globalState.getCurrencyFor(entity.getUUID());
				renderPlayerCurrency(event, entity, currency);
			}
		}
	}

	private static void renderPlayerCurrency(RenderNameTagEvent event, Entity entity, int currency) {
		final EntityRenderDispatcher renderDispatcher = CLIENT.getEntityRenderDispatcher();
		double distance2 = renderDispatcher.distanceToSqr(entity);
		if (!ForgeHooksClient.isNameplateInRenderDistance(entity, distance2) || entity.isDiscrete()) {
			return;
		}

		if (entity == CLIENT.cameraEntity || !Minecraft.renderNames()) {
			return;
		}

		final float itemSize = 16.0F;
		final float textScale = 1.0F / 2.5F;

		String currencyText = String.valueOf(currency);

		PoseStack poseStack = event.getPoseStack();
		MultiBufferSource buffer = event.getMultiBufferSource();
		int packedLight = event.getPackedLight();

		Font font = event.getEntityRenderer().getFont();
		ItemRenderer items = CLIENT.getItemRenderer();

		float left = -(font.width(currencyText) * textScale + itemSize) / 2.0F;

		poseStack.pushPose();
		poseStack.translate(0.0, entity.getBbHeight() + 0.75, 0.0);
		poseStack.mulPose(renderDispatcher.cameraOrientation());
		poseStack.scale(0.0625F * textScale, 0.0625F * textScale, 0.0625F * textScale);

		poseStack.pushPose();
		poseStack.scale(-1.0F, -1.0F, 1.0F);

		float textX = (left + itemSize) * textScale;
		float textY = -font.lineHeight / 2.0F;
		font.drawInBatch(currencyText, textX, textY, CommonColors.WHITE, false, poseStack.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, packedLight);
		poseStack.popPose();

		poseStack.pushPose();
		poseStack.translate(-left, 0.0F, 0.0F);
		poseStack.scale(-itemSize, itemSize, -itemSize);
		items.renderStatic(ClientGameStateManager.getOrNull(BiodiversityBlitz.CURRENCY_ITEM).item(), ItemDisplayContext.GUI, packedLight, OverlayTexture.NO_OVERLAY, poseStack, buffer, CLIENT.level, 0);
		poseStack.popPose();

		poseStack.popPose();
	}
}
