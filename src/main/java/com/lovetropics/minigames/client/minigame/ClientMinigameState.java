package com.lovetropics.minigames.client.minigame;

import java.util.Optional;

import javax.annotation.Nullable;

import com.lovetropics.minigames.common.minigames.MinigameStatus;
import com.lovetropics.minigames.common.minigames.PlayerRole;

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
	private @Nullable PlayerRole role;

	public ClientMinigameState(ResourceLocation minigame, String unlocName, MinigameStatus status) {
		this(minigame, unlocName, status, null);
	}

	public ClientMinigameState(ResourceLocation minigame, String unlocName, MinigameStatus status, @Nullable PlayerRole role) {
		this.minigame = minigame;
		this.unlocName = unlocName;
		this.status = status;
		this.role = role;
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

	public @Nullable PlayerRole getRole() {
		return role;
	}

	public void setRole(@Nullable PlayerRole role) {
		this.role = role;
	}
}
