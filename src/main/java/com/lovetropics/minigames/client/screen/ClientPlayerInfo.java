package com.lovetropics.minigames.client.screen;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.UUID;

public final class ClientPlayerInfo {
	private static final Minecraft CLIENT = Minecraft.getInstance();

	@Nullable
	public static GameProfile getPlayerProfile(UUID uuid) {
		PlayerInfo info = get(uuid);
		return info != null ? info.getProfile() : null;
	}

	@Nullable
	public static Component getName(UUID uuid) {
		PlayerInfo info = get(uuid);
		if (info != null) {
			Component displayName = info.getTabListDisplayName();
			return displayName != null ? displayName : Component.literal(info.getProfile().getName());
		} else {
			return null;
		}
	}

	@Nullable
	public static ResourceLocation getSkin(UUID uuid) {
		PlayerInfo info = get(uuid);
		return info != null ? info.getSkinLocation() : null;
	}

	@Nullable
	public static PlayerInfo get(UUID uuid) {
		ClientPacketListener connection = CLIENT.getConnection();
		return connection != null ? connection.getPlayerInfo(uuid) : null;
	}
}
