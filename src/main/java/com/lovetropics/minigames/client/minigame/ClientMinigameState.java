package com.lovetropics.minigames.client.minigame;

import com.lovetropics.minigames.common.core.game.GameStatus;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Optional;

public class ClientMinigameState {

	private static final Int2ObjectMap<ClientMinigameState> GAMES = new Int2ObjectOpenHashMap<>();
	private static ClientMinigameState currentGame;

	public static Collection<ClientMinigameState> getGames() {
		return GAMES.values();
	}

	public static Optional<ClientMinigameState> get(int instanceId) {
		return Optional.ofNullable(GAMES.get(instanceId));
	}

	@Nullable
	public static ClientMinigameState getCurrent() {
		return currentGame;
	}

	static void set(int instanceId, @Nullable ClientMinigameState state) {
		if (state != null) {
			GAMES.put(instanceId, state);

			if (state.role != null) {
				currentGame = state;
			}
		} else {
			GAMES.remove(instanceId);

			ClientMinigameState currentGame = ClientMinigameState.currentGame;
			if (currentGame != null && currentGame.instanceId == instanceId) {
				ClientMinigameState.currentGame = null;
			}
		}
	}

	public static void clear() {
		GAMES.clear();
		currentGame = null;
	}

	static void update(int instanceId, ClientMinigameState state) {
		if (state != null) {
			get(instanceId).ifPresent(current -> {
				state.role = current.role;
				state.members = current.members;
			});
		}
		set(instanceId, state);
	}

	private final int instanceId;
	private final ResourceLocation minigame;
	private final String unlocName;
	private final GameStatus status;
	private final int maxPlayers;
	private @Nullable PlayerRole role;

	private EnumMap<PlayerRole, Integer> members = new EnumMap<>(PlayerRole.class);

	public ClientMinigameState(int instanceId, ResourceLocation minigame, String unlocName, GameStatus status, int maxPlayers) {
		this.instanceId = instanceId;
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

	public GameStatus getStatus() {
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
