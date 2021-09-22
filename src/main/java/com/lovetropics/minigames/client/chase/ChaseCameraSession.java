package com.lovetropics.minigames.client.chase;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.EntityViewRenderEvent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

class ChaseCameraSession {
	private static final Minecraft CLIENT = Minecraft.getInstance();

	List<UUID> players;

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

	@Nullable
	GameProfile getPlayerProfile(UUID uuid) {
		NetworkPlayerInfo playerInfo = this.getPlayerInfo(uuid);
		return playerInfo != null ? playerInfo.getGameProfile() : null;
	}

	@Nullable
	private NetworkPlayerInfo getPlayerInfo(UUID uuid) {
		ClientPlayNetHandler connection = CLIENT.getConnection();
		return connection != null ? connection.getPlayerInfo(uuid) : null;
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
