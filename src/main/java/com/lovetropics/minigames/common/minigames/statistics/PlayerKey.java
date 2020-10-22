package com.lovetropics.minigames.common.minigames.statistics;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.minecraft.entity.player.PlayerEntity;

import java.net.Proxy;
import java.util.UUID;

public final class PlayerKey {
	private static final YggdrasilAuthenticationService AUTH_SERVICE = new YggdrasilAuthenticationService(Proxy.NO_PROXY, UUID.randomUUID().toString());
	private static final MinecraftSessionService SESSION_SERVICE = AUTH_SERVICE.createMinecraftSessionService();

	private final GameProfile profile;

	private PlayerKey(GameProfile profile) {
		this.profile = profile;
	}

	public static PlayerKey from(GameProfile profile) {
		return new PlayerKey(profile);
	}

	public static PlayerKey from(PlayerEntity player) {
		return new PlayerKey(player.getGameProfile());
	}

	public UUID getId() {
		return profile.getId();
	}

	public String getName() {
		return profile.getName();
	}

	@Override
	public String toString() {
		return profile.getName();
	}

	public JsonElement serializeProfile() {
		JsonObject root = new JsonObject();
		root.addProperty("id", profile.getId().toString());
		root.addProperty("name", profile.getName());

		MinecraftProfileTexture skinTexture = SESSION_SERVICE.getTextures(profile, true).get(MinecraftProfileTexture.Type.SKIN);
		if (skinTexture != null) {
			JsonObject skinRoot = new JsonObject();
			skinRoot.addProperty("url", skinTexture.getUrl());

			String model = skinTexture.getMetadata("model");
			if (model != null) {
				skinRoot.addProperty("model", model);
			}
		}

		return root;
	}

	public JsonElement serializeId() {
		return new JsonPrimitive(profile.getId().toString());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;

		if (obj instanceof PlayerKey) {
			PlayerKey key = (PlayerKey) obj;
			return profile.getId().equals(key.profile.getId());
		}

		return false;
	}

	@Override
	public int hashCode() {
		return profile.getId().hashCode();
	}
}
