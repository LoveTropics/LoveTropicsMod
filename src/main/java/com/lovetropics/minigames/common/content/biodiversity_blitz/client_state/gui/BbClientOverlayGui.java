package com.lovetropics.minigames.common.content.biodiversity_blitz.client_state.gui;

import com.google.common.base.Suppliers;
import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.client.game.ClientGameStateManager;
import com.lovetropics.minigames.common.content.biodiversity_blitz.BiodiversityBlitz;
import com.lovetropics.minigames.common.content.biodiversity_blitz.client_state.ClientBiodiversityBlitzState;
import com.lovetropics.minigames.common.content.biodiversity_blitz.client_state.CurrencyTargetState;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = Constants.MODID, value = Dist.CLIENT)
public final class BbClientOverlayGui {
	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static final int PADDING = 2;

	// TODO: hardcoded
	private static final Supplier<ItemStack> CURRENCY_ITEM = Suppliers.memoize(() -> new ItemStack(BiodiversityBlitz.OSA_POINT.get()));
	private static final int ITEM_SIZE = 16;

	@SubscribeEvent
	public static void renderGameOverlay(RenderGameOverlayEvent event) {
		if (event.getType() != RenderGameOverlayEvent.ElementType.TEXT) {
			return;
		}

		ClientBiodiversityBlitzState state = ClientGameStateManager.getOrNull(BiodiversityBlitz.CLIENT_STATE);
		if (state != null) {
			CurrencyTargetState currencyTarget = ClientGameStateManager.getOrNull(BiodiversityBlitz.CURRENCY_TARGET);
			render(event, state, currencyTarget);
		}
	}

	private static void render(RenderGameOverlayEvent event, ClientBiodiversityBlitzState state, CurrencyTargetState currencyTarget) {
		MatrixStack matrixStack = event.getMatrixStack();
		FontRenderer font = CLIENT.fontRenderer;

		final int left = PADDING;
		final int top = PADDING;

		int x = left;
		int y = top;

		renderItem(matrixStack, CURRENCY_ITEM.get(), x, y);

		String currency = String.valueOf(state.getCurrency());
		if (currencyTarget != null) {
			currency = currency + TextFormatting.GRAY + "/" + currencyTarget.getValue();
		}

		font.drawStringWithShadow(
				matrixStack, currency,
				x + ITEM_SIZE + PADDING,
				y + (ITEM_SIZE - font.FONT_HEIGHT) / 2,
				0xFFFFFFFF
		);
		y += ITEM_SIZE + PADDING;

		int increment = state.getNextIncrement();
		// Increment can never be 0 otherwise, as 2 osas per drop is guaranteed
		boolean isInPlot = increment > 0;
		TextFormatting incrementColor = isInPlot ? TextFormatting.AQUA : TextFormatting.RED;

		String nextCurrencyIncrement = incrementColor + "+" + increment + TextFormatting.GRAY + " next drop";
		font.drawStringWithShadow(matrixStack, nextCurrencyIncrement, x, y, 0xFFFFFFFF);

		if (!isInPlot) {
			font.drawStringWithShadow(matrixStack, TextFormatting.GRAY + "Get back in your plot to receive osas!", x, y + font.FONT_HEIGHT, 0xFFFFFFFF);
		}
	}

	private static void renderItem(MatrixStack matrixStack, ItemStack stack, int x, int y) {
		RenderSystem.pushMatrix();
		RenderSystem.multMatrix(matrixStack.getLast().getMatrix());
		CLIENT.getItemRenderer().renderItemIntoGUI(stack, x, y);
		RenderSystem.popMatrix();
	}
}
