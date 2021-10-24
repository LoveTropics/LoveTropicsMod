package com.lovetropics.minigames.common.content.biodiversity_blitz.client_tweak.gui;

import com.google.common.base.Suppliers;
import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.client.tweaks.ClientGameTweaksState;
import com.lovetropics.minigames.common.content.biodiversity_blitz.BiodiversityBlitz;
import com.lovetropics.minigames.common.content.biodiversity_blitz.client_tweak.ClientBiodiversityBlitzState;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
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

		ClientBiodiversityBlitzState display = ClientGameTweaksState.getOrNull(BiodiversityBlitz.CLIENT_STATE_TWEAK);
		if (display != null) {
			render(event, display);
		}
	}

	private static void render(RenderGameOverlayEvent event, ClientBiodiversityBlitzState display) {
		MatrixStack matrixStack = event.getMatrixStack();
		FontRenderer font = CLIENT.fontRenderer;

		final int left = PADDING;
		final int top = PADDING;

		int x = left;
		int y = top;

		renderItem(matrixStack, CURRENCY_ITEM.get(), x, y);
		x += ITEM_SIZE + PADDING;

		String currency = String.valueOf(display.getCurrency());
		font.drawStringWithShadow(matrixStack, currency, x, y + (ITEM_SIZE - font.FONT_HEIGHT) / 2, 0xFFFFFFFF);
	}

	private static void renderItem(MatrixStack matrixStack, ItemStack stack, int x, int y) {
		RenderSystem.pushMatrix();
		RenderSystem.multMatrix(matrixStack.getLast().getMatrix());
		CLIENT.getItemRenderer().renderItemIntoGUI(stack, x, y);
		RenderSystem.popMatrix();
	}
}
