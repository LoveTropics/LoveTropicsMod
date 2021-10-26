package com.lovetropics.minigames.client.lobby.state.message;

import com.lovetropics.minigames.client.lobby.state.ClientCurrentGame;
import com.lovetropics.minigames.client.lobby.state.ClientLobbyManager;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class LobbyUpdateMessage {
	private final int id;
	@Nullable
	private final Update update;

	private LobbyUpdateMessage(int id, @Nullable Update update) {
		this.id = id;
		this.update = update;
	}

	public static LobbyUpdateMessage update(IGameLobby lobby) {
		int id = lobby.getMetadata().id().networkId();
		String name = lobby.getMetadata().name();
		ClientCurrentGame currentGame = lobby.getClientCurrentGame();
		return new LobbyUpdateMessage(id, new Update(name, currentGame));
	}

	public static LobbyUpdateMessage remove(IGameLobby lobby) {
		return new LobbyUpdateMessage(lobby.getMetadata().id().networkId(), null);
	}

	public void encode(PacketBuffer buffer) {
		buffer.writeVarInt(id);

		buffer.writeBoolean(update != null);
		if (update != null) {
			update.encode(buffer);
		}
	}

	public static LobbyUpdateMessage decode(PacketBuffer buffer) {
		int id = buffer.readVarInt();
		if (buffer.readBoolean()) {
			Update update = Update.decode(buffer);
			return new LobbyUpdateMessage(id, update);
		}
		return new LobbyUpdateMessage(id, null);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			if (update != null) {
				ClientLobbyManager.addOrUpdate(id, update.name, update.currentGame);
			} else {
				ClientLobbyManager.remove(id);
			}
		});
		ctx.get().setPacketHandled(true);
	}

	static final class Update {
		final String name;
		@Nullable
		final ClientCurrentGame currentGame;

		Update(String name, @Nullable ClientCurrentGame currentGame) {
			this.name = name;
			this.currentGame = currentGame;
		}

		static Update decode(PacketBuffer buffer) {
			String name = buffer.readString(200);

			ClientCurrentGame currentGame = null;
			if (buffer.readBoolean()) {
				currentGame = ClientCurrentGame.decode(buffer);
			}

			return new Update(name, currentGame);
		}

		void encode(PacketBuffer buffer) {
			buffer.writeString(name, 200);

			buffer.writeBoolean(currentGame != null);
			if (currentGame != null) {
				currentGame.encode(buffer);
			}
		}
	}
}
