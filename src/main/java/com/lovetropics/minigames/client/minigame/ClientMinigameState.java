package com.lovetropics.minigames.client.minigame;

import java.util.Optional;

import com.lovetropics.minigames.common.minigames.MinigameStatus;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

public class ClientMinigameState {

	private static ClientMinigameState currentState;

	public static Optional<ClientMinigameState> get() {
		return Optional.ofNullable(currentState);
	}

	static void set(ClientMinigameState state) {
		currentState = state;
	}

	private final ResourceLocation minigame;
	private final String unlocName;
	private final MinigameStatus status;
	private boolean joined;

	public ClientMinigameState(ResourceLocation minigame, String unlocName, MinigameStatus status) {
		this(minigame, unlocName, status, false);
	}

	public ClientMinigameState(ResourceLocation minigame, String unlocName, MinigameStatus status, boolean joined) {
		this.minigame = minigame;
		this.unlocName = unlocName;
		this.status = status;
		this.joined = joined;
	}

	public ResourceLocation getMinigame() {
		return minigame;
	}

	public String getDisplayName() {
		return I18n.format(unlocName);
	}

	public MinigameStatus getStatus() {
		return status;
	}

	public boolean isJoined() {
		return joined;
	}

	public void setJoined(boolean joined) {
		this.joined = joined;
	}
}
