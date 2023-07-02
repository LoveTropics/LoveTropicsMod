package com.lovetropics.minigames.client.lobby.state.message;

import com.lovetropics.minigames.client.lobby.state.ClientCurrentGame;
import com.lovetropics.minigames.client.lobby.state.ClientLobbyManager;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public record LobbyUpdateMessage(int id, @Nullable Update update) {
	public static LobbyUpdateMessage update(IGameLobby lobby) {
		int id = lobby.getMetadata().id().networkId();
		String name = lobby.getMetadata().name();
		ClientCurrentGame currentGame = lobby.getClientCurrentGame();
		return new LobbyUpdateMessage(id, new Update(name, currentGame));
	}

	public static LobbyUpdateMessage remove(IGameLobby lobby) {
		return new LobbyUpdateMessage(lobby.getMetadata().id().networkId(), null);
	}

	public void encode(FriendlyByteBuf buffer) {
		buffer.writeVarInt(id);
		buffer.writeNullable(update, (b, u) -> u.encode(b));
	}

	public static LobbyUpdateMessage decode(FriendlyByteBuf buffer) {
		int id = buffer.readVarInt();
		Update update = buffer.readNullable(Update::decode);
		return new LobbyUpdateMessage(id, update);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		if (update != null) {
			ClientLobbyManager.addOrUpdate(id, update.name, update.currentGame);
		} else {
			ClientLobbyManager.remove(id);
		}
	}

	private record Update(String name, @Nullable ClientCurrentGame currentGame) {
		private static final int MAX_NAME_LENGTH = 200;

		private static Update decode(FriendlyByteBuf buffer) {
			String name = buffer.readUtf(MAX_NAME_LENGTH);
			ClientCurrentGame currentGame = buffer.readNullable(ClientCurrentGame::decode);
			return new Update(name, currentGame);
		}

		private void encode(FriendlyByteBuf buffer) {
			buffer.writeUtf(name, MAX_NAME_LENGTH);
			buffer.writeNullable(currentGame, (b, g) -> g.encode(b));
		}
	}
}
