package com.lovetropics.minigames.client.minigame;

import com.lovetropics.minigames.common.core.game.GameStatus;
import com.lovetropics.minigames.common.core.game.lobby.GameLobbyId;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientGameLobbyMessage {

	private final int lobbyId;
	private final ResourceLocation minigame;
	private final String unlocName;
	private final GameStatus status;
	private final int maxPlayers;

	public static ClientGameLobbyMessage update(IGameLobby lobby) {
		// TODO
		return new ClientGameLobbyMessage(lobby.getMetadata().id().networkId(), new ResourceLocation("a"), "", GameStatus.ACTIVE, 1);
	}

	public static ClientGameLobbyMessage stop(GameLobbyId lobbyId) {
		return new ClientGameLobbyMessage(lobbyId.networkId(), null, null, null, 0);
	}

	private ClientGameLobbyMessage(int lobbyId, ResourceLocation minigame, String unlocName, GameStatus status, int maxPlayers) {
		this.lobbyId = lobbyId;
		this.minigame = minigame;
		this.unlocName = unlocName;
		this.status = status;
		this.maxPlayers = maxPlayers;
	}

	public void encode(PacketBuffer buffer) {
		buffer.writeVarInt(lobbyId);
		buffer.writeBoolean(minigame != null);
		if (minigame != null) {
			buffer.writeResourceLocation(minigame);
			buffer.writeString(unlocName, 200);
			buffer.writeEnumValue(status);
			buffer.writeInt(maxPlayers);
		}
	}

	public static ClientGameLobbyMessage decode(PacketBuffer buffer) {
		int instanceId = buffer.readVarInt();
		if (buffer.readBoolean()) {
			ResourceLocation minigame = buffer.readResourceLocation();
			String unlocName = buffer.readString(200);
			GameStatus status = buffer.readEnumValue(GameStatus.class);
			int maxPlayers = buffer.readInt();
			return new ClientGameLobbyMessage(instanceId, minigame, unlocName, status, maxPlayers);
		}
		return new ClientGameLobbyMessage(instanceId, null, null, null, 0);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ClientMinigameState state = minigame == null ? null : new ClientMinigameState(lobbyId, minigame, unlocName, status, maxPlayers);
			ClientMinigameState.update(lobbyId, state);
		});
		ctx.get().setPacketHandled(true);
	}
}
