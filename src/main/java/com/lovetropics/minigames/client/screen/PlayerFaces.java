package com.lovetropics.minigames.client.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.UUID;

public final class PlayerFaces {
	private static final Minecraft CLIENT = Minecraft.getInstance();

	public static void render(UUID uuid, MatrixStack matrixStack, int x, int y, int size) {
		NetworkPlayerInfo playerInfo = getPlayerInfo(uuid);
		ResourceLocation skin = playerInfo != null ? playerInfo.getLocationSkin() : getDefaultSkin(uuid);

		CLIENT.getTextureManager().bindTexture(skin);
		AbstractGui.blit(matrixStack, x, y, size, size, 8.0F, 8.0F, 8, 8, 64, 64);
		AbstractGui.blit(matrixStack, x, y, size, size, 40.0F, 8.0F, 8, 8, 64, 64);
	}

	@Nullable
	private static NetworkPlayerInfo getPlayerInfo(UUID uuid) {
		ClientPlayNetHandler connection = CLIENT.getConnection();
		return connection != null ? connection.getPlayerInfo(uuid) : null;
	}

	private static ResourceLocation getDefaultSkin(UUID uuid) {
		return DefaultPlayerSkin.getDefaultSkin(uuid);
	}
}
