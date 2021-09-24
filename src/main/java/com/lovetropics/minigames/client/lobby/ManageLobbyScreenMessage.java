package com.lovetropics.minigames.client.lobby;

import com.lovetropics.minigames.client.lobby.screen.ManageLobbyScreen;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class ManageLobbyScreenMessage {
	private final int lobbyId;
	private final String name;
	private final List<ClientQueuedGame> queue;

	private final List<ClientGameDefinition> installedGames;

	private ManageLobbyScreenMessage(
			int lobbyId, String name,
			List<ClientQueuedGame> queue,
			List<ClientGameDefinition> installedGames
	) {
		this.lobbyId = lobbyId;
		this.name = name;
		this.queue = queue;
		this.installedGames = installedGames;
	}

	public static ManageLobbyScreenMessage create(IGameLobby lobby) {
		int networkId = lobby.getMetadata().id().networkId();
		String name = lobby.getMetadata().name();
		List<ClientQueuedGame> queue = lobby.getGameQueue().clientEntries();

		return new ManageLobbyScreenMessage(networkId, name, queue, ClientGameDefinition.installed());
	}

	public void encode(PacketBuffer buffer) {
		buffer.writeVarInt(this.lobbyId);

		buffer.writeString(this.name);

		buffer.writeVarInt(this.queue.size());
		for (ClientQueuedGame entry : this.queue) {
			entry.encode(buffer);
		}

		buffer.writeVarInt(this.installedGames.size());
		for (ClientGameDefinition game : this.installedGames) {
			game.encode(buffer);
		}
	}

	public static ManageLobbyScreenMessage decode(PacketBuffer buffer) {
		int lobbyId = buffer.readVarInt();

		String name = buffer.readString();

		int queueSize = buffer.readVarInt();
		List<ClientQueuedGame> queue = new ArrayList<>(queueSize);
		for (int i = 0; i < queueSize; i++) {
			queue.add(ClientQueuedGame.decode(buffer));
		}

		int installedSize = buffer.readVarInt();
		List<ClientGameDefinition> installedGames = new ArrayList<>(installedSize);
		for (int i = 0; i < installedSize; i++) {
			installedGames.add(ClientGameDefinition.decode(buffer));
		}

		return new ManageLobbyScreenMessage(lobbyId, name, queue, installedGames);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			openScreen(name, queue, installedGames);
		});
		ctx.get().setPacketHandled(true);
	}

	// TODO: dedicated server
	private static void openScreen(String name, List<ClientQueuedGame> queue, List<ClientGameDefinition> installedGames) {
		Minecraft.getInstance().displayGuiScreen(new ManageLobbyScreen(name, queue, installedGames));
	}
}
