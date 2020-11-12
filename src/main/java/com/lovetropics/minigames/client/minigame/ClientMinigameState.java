package com.lovetropics.minigames.client.minigame;

import java.util.EnumMap;
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

	static void update(ClientMinigameState state) {
		get().ifPresent(current -> {
			state.role = current.role;
			state.members = current.members;
		});
		set(state);
	}

	private final ResourceLocation minigame;
	private final String unlocName;
	private final MinigameStatus status;
	private final int maxPlayers;
	private @Nullable PlayerRole role;
	private EnumMap<PlayerRole, Integer> members = new EnumMap<>(PlayerRole.class);

	public ClientMinigameState(ResourceLocation minigame, String unlocName, MinigameStatus status, int maxPlayers) {
		this.minigame = minigame;
		this.unlocName = unlocName;
		this.status = status;
		this.maxPlayers = maxPlayers;
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

	public int getMaxPlayers() {
		return maxPlayers;
	}

	public @Nullable PlayerRole getRole() {
		return role;
	}

	public void setRole(@Nullable PlayerRole role) {
		this.role = role;
	}

	public void setMemberCount(PlayerRole role, int count) {
		this.members.put(role, count);
	}

	public int getMemberCount(PlayerRole role) {
		return this.members.getOrDefault(role, 0);
	}
}
