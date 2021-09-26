package com.lovetropics.minigames.client.lobby;

import com.lovetropics.minigames.client.lobby.screen.ManageLobbyScreen;
import com.lovetropics.minigames.client.lobby.state.ClientGameDefinition;
import com.lovetropics.minigames.client.lobby.state.ClientLobbyManager;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class ManageLobbyScreenMessage {
	private final int id;

	private final List<ClientGameDefinition> installedGames;

	private ManageLobbyScreenMessage(int id, List<ClientGameDefinition> installedGames) {
		this.id = id;
		this.installedGames = installedGames;
	}

	public static ManageLobbyScreenMessage create(IGameLobby lobby) {
		int networkId = lobby.getMetadata().id().networkId();
		return new ManageLobbyScreenMessage(networkId, ClientGameDefinition.installed());
	}

	public void encode(PacketBuffer buffer) {
		buffer.writeVarInt(this.id);

		buffer.writeVarInt(this.installedGames.size());
		for (ClientGameDefinition game : this.installedGames) {
			game.encode(buffer);
		}
	}

	public static ManageLobbyScreenMessage decode(PacketBuffer buffer) {
		int id = buffer.readVarInt();

		int installedSize = buffer.readVarInt();
		List<ClientGameDefinition> installedGames = new ArrayList<>(installedSize);
		for (int i = 0; i < installedSize; i++) {
			installedGames.add(ClientGameDefinition.decode(buffer));
		}

		return new ManageLobbyScreenMessage(id, installedGames);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			openScreen(id, installedGames);
		});
		ctx.get().setPacketHandled(true);
	}

	// TODO: dedicated server
	private static void openScreen(int id, List<ClientGameDefinition> installedGames) {
		ClientLobbyManager.get(id).ifPresent(state -> {
			Minecraft.getInstance().displayGuiScreen(new ManageLobbyScreen(state, installedGames));
		});
	}
}
