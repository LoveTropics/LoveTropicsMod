package com.lovetropics.minigames.client.chase;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.EntityViewRenderEvent;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

class ChaseCameraSession {
	private static final Minecraft CLIENT = Minecraft.getInstance();

	List<UUID> players;
	private final Map<UUID, GameProfile> playerProfiles = new HashMap<>();
	private final Map<UUID, ResourceLocation> playerSkins = new HashMap<>();

	final ChaseCameraUi ui;

	ChaseCameraState state;
	ChaseCameraState.StateApplicator stateApplicator;

	double targetZoom = 1.0;
	double zoom = 1.0;
	double prevZoom = 1.0;

	ChaseCameraSession(List<UUID> players) {
		this.players = players;
		this.ui = new ChaseCameraUi(this);
		this.applyState(ChaseCameraState.FREE_CAMERA);
	}

	ResourceLocation getPlayerSkin(UUID id) {
		GameProfile profile = getPlayerProfile(id);
		if (profile == null) {
			return DefaultPlayerSkin.getDefaultSkin(id);
		}

		return getPlayerSkin(profile);
	}

	ResourceLocation getPlayerSkin(GameProfile profile) {
		ResourceLocation existingSkin = playerSkins.get(profile.getId());
		if (existingSkin != null) {
			return existingSkin;
		}

		ResourceLocation skin = loadPlayerSkin(profile);
		playerSkins.put(profile.getId(), skin);

		return skin;
	}

	@Nullable
	GameProfile getPlayerProfile(UUID id) {
		GameProfile existingProfile = playerProfiles.get(id);
		if (existingProfile != null) {
			return existingProfile;
		}

		PlayerEntity player = CLIENT.world.getPlayerByUuid(id);
		if (player == null) {
			return null;
		}

		GameProfile profile = player.getGameProfile();
		playerProfiles.put(id, profile);

		return profile;
	}

	private ResourceLocation loadPlayerSkin(GameProfile profile) {
		SkinManager skinManager = CLIENT.getSkinManager();

		Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> textures = skinManager.loadSkinFromCache(profile);
		if (textures.containsKey(MinecraftProfileTexture.Type.SKIN)) {
			return skinManager.loadSkin(textures.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN);
		} else {
			return DefaultPlayerSkin.getDefaultSkin(PlayerEntity.getUUID(profile));
		}
	}

	void tick() {
		prevZoom = zoom;
		zoom = targetZoom;

		if (stateApplicator != null) {
			if (stateApplicator.tryApply(CLIENT)) {
				stateApplicator = null;
			} else {
				return;
			}
		}

		ChaseCameraState newState = state.tick(CLIENT, this, CLIENT.player);
		if (!newState.equals(state)) {
			applyState(newState);
			ui.updateState(newState);
		}
	}

	double getZoom(float partialTicks) {
		return MathHelper.lerp(partialTicks, prevZoom, zoom);
	}

	void renderTick() {
		state.renderTick(CLIENT, this, CLIENT.player);
	}

	void applyToCamera(ActiveRenderInfo camera, float partialTicks, EntityViewRenderEvent.CameraSetup event) {
		state.applyToCamera(CLIENT, this, CLIENT.player, camera, partialTicks, event);
	}

	void applyState(ChaseCameraState state) {
		this.state = state;
		this.stateApplicator = state.apply(CLIENT, this);
	}

	void updatePlayers(List<UUID> players) {
		this.players = players;
		ui.updatePlayers(players);
	}

	void close() {
		applyState(ChaseCameraState.FREE_CAMERA);
	}
}
