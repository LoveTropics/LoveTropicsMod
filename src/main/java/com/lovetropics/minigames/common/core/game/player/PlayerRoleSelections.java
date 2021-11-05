package com.lovetropics.minigames.common.core.game.player;

import com.lovetropics.minigames.client.lobby.select_role.SelectRolePromptMessage;
import com.lovetropics.minigames.common.core.game.lobby.GameLobbyId;
import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.fml.network.PacketDistributor;

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
		for (ServerPlayerEntity player : players) {
			this.prompt(player);
		}
	}

	public CompletableFuture<PlayerRole> prompt(ServerPlayerEntity player) {
		CompletableFuture<PlayerRole> future = this.pendingResponses.get(player.getUniqueID());
		if (future == null) {
			this.pendingResponses.put(player.getUniqueID(), future = new CompletableFuture<>());

			future.thenAccept(role -> {
				this.roles.put(player.getUniqueID(), role);
			});
		}

		this.sendPromptTo(player);

		return future;
	}

	private void sendPromptTo(ServerPlayerEntity player) {
		LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SelectRolePromptMessage(lobbyId.networkId()));
		player.playSound(SoundEvents.ENTITY_ARROW_HIT_PLAYER, SoundCategory.MASTER, 1.0F, 1.0F);
	}

	public void remove(ServerPlayerEntity player) {
		this.roles.remove(player.getUniqueID());
	}

	public void acceptResponse(ServerPlayerEntity player, PlayerRole role) {
		CompletableFuture<PlayerRole> future = this.pendingResponses.remove(player.getUniqueID());
		if (future != null) {
			future.complete(role);
		}
	}

	@Nonnull
	public PlayerRole getSelectedRoleFor(ServerPlayerEntity player) {
		return roles.getOrDefault(player.getUniqueID(), PlayerRole.SPECTATOR);
	}

	public boolean hasPending() {
		return pendingResponses.isEmpty();
	}
}
