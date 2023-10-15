package com.lovetropics.minigames.common.core.game.player;

import com.lovetropics.minigames.client.lobby.select_role.SelectRolePromptMessage;
import com.lovetropics.minigames.common.core.game.lobby.GameLobbyId;
import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class PlayerRoleSelections {
	private final GameLobbyId lobbyId;

	private final Map<UUID, PlayerRole> roles = new Object2ObjectOpenHashMap<>();
	private final Map<UUID, CompletableFuture<PlayerRole>> pendingResponses = new Object2ObjectOpenHashMap<>();

	public PlayerRoleSelections(GameLobbyId lobbyId) {
		this.lobbyId = lobbyId;
	}

	public void clear() {
		this.roles.clear();
		this.pendingResponses.clear();
	}

	public void clearAndPromptAll(PlayerSet players) {
		this.clear();
		for (ServerPlayer player : players) {
			this.prompt(player);
		}
	}

	public CompletableFuture<PlayerRole> prompt(ServerPlayer player) {
		CompletableFuture<PlayerRole> future = this.pendingResponses.get(player.getUUID());
		if (future == null) {
			this.pendingResponses.put(player.getUUID(), future = new CompletableFuture<>());

			future.thenAccept(role -> {
				this.roles.put(player.getUUID(), role);
			});
		}

		this.sendPromptTo(player);

		return future;
	}

	private void sendPromptTo(ServerPlayer player) {
		LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SelectRolePromptMessage(lobbyId.networkId()));
		player.playNotifySound(SoundEvents.ARROW_HIT_PLAYER, SoundSource.MASTER, 1.0F, 1.0F);
	}

	public void remove(ServerPlayer player) {
		this.roles.remove(player.getUUID());
	}

	public void acceptResponse(ServerPlayer player, PlayerRole role) {
		CompletableFuture<PlayerRole> future = this.pendingResponses.remove(player.getUUID());
		if (future != null) {
			future.complete(role);
		}
	}

	public void setRole(ServerPlayer player, PlayerRole role) {
		this.roles.put(player.getUUID(), role);
	}

	@Nonnull
	public PlayerRole getSelectedRoleFor(ServerPlayer player) {
		return roles.getOrDefault(player.getUUID(), PlayerRole.SPECTATOR);
	}

	public boolean hasPending() {
		return pendingResponses.isEmpty();
	}

	@Override
	public String toString() {
		return this.roles.toString();
	}
}
