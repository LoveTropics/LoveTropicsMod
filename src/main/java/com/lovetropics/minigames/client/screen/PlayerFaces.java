package com.lovetropics.minigames.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public final class PlayerFaces {
	private static final Minecraft CLIENT = Minecraft.getInstance();

	public static void render(UUID uuid, PoseStack matrixStack, int x, int y, int size) {
		ResourceLocation skin = ClientPlayerInfo.getSkin(uuid);
		if (skin == null) {
			skin = DefaultPlayerSkin.getDefaultSkin(uuid);
		}

		CLIENT.getTextureManager().bind(skin);
		GuiComponent.blit(matrixStack, x, y, size, size, 8.0F, 8.0F, 8, 8, 64, 64);
		GuiComponent.blit(matrixStack, x, y, size, size, 40.0F, 8.0F, 8, 8, 64, 64);
	}
}
