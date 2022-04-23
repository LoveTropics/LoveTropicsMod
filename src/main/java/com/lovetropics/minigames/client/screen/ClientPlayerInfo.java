package com.lovetropics.minigames.client.screen;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nullable;
import java.util.UUID;

public final class ClientPlayerInfo {
	private static final Minecraft CLIENT = Minecraft.getInstance();

	@Nullable
	public static GameProfile getPlayerProfile(UUID uuid) {
		NetworkPlayerInfo info = get(uuid);
		return info != null ? info.getProfile() : null;
	}

	@Nullable
	public static ITextComponent getName(UUID uuid) {
		NetworkPlayerInfo info = get(uuid);
		if (info != null) {
			ITextComponent displayName = info.getTabListDisplayName();
			return displayName != null ? displayName : new StringTextComponent(info.getProfile().getName());
		} else {
			return null;
		}
	}

	@Nullable
	public static ResourceLocation getSkin(UUID uuid) {
		NetworkPlayerInfo info = get(uuid);
		return info != null ? info.getSkinLocation() : null;
	}

	@Nullable
	public static NetworkPlayerInfo get(UUID uuid) {
		ClientPlayNetHandler connection = CLIENT.getConnection();
		return connection != null ? connection.getPlayerInfo(uuid) : null;
	}
}
