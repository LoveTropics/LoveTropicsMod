package com.lovetropics.minigames.common.content.biodiversity_blitz.client_state.render;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.client.game.ClientGameStateManager;
import com.lovetropics.minigames.common.content.biodiversity_blitz.BiodiversityBlitz;
import com.lovetropics.minigames.common.content.biodiversity_blitz.client_state.ClientBbSelfState;
import com.lovetropics.minigames.common.content.biodiversity_blitz.client_state.CurrencyTargetState;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.CommonColors;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import javax.annotation.Nullable;

public final class BbClientRenderEffects {
	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static final int PADDING = 2;

	private static final int ITEM_SIZE = 16;

	public static void registerOverlays(RegisterGuiLayersEvent event) {
		event.registerBelow(VanillaGuiLayers.DEBUG_OVERLAY, LoveTropics.location("biodiversity_blitz"), (graphics, deltaTracker) -> {
			if (Minecraft.getInstance().options.hideGui) {
				return;
			}
			ClientBbSelfState selfState = ClientGameStateManager.getOrNull(BiodiversityBlitz.SELF_STATE);
			if (selfState != null) {
				CurrencyTargetState currencyTarget = ClientGameStateManager.getOrNull(BiodiversityBlitz.CURRENCY_TARGET);
				renderOverlay(graphics, selfState, currencyTarget);
			}
		});
	}

	private static void renderOverlay(GuiGraphics graphics, ClientBbSelfState selfState, @Nullable CurrencyTargetState currencyTarget) {
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
}
