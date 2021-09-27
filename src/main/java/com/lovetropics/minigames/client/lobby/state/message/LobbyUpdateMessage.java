package com.lovetropics.minigames.client.lobby.state.message;

import com.lovetropics.minigames.client.lobby.state.ClientGameDefinition;
import com.lovetropics.minigames.client.lobby.state.ClientLobbyManager;
import com.lovetropics.minigames.client.lobby.state.ClientQueuedGame;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

// TODO: only send for initiator & split out the current game management
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
		List<ClientQueuedGame> queue = lobby.getGameQueue().clientEntries();
		ClientGameDefinition definition = lobby.getCurrentGame() != null ? ClientGameDefinition.from(lobby.getCurrentGame().getDefinition()) : null;
		return new LobbyUpdateMessage(id, new Update(name, queue, definition));
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
				ClientLobbyManager.addOrUpdate(id, update.name, update.queue, update.currentGame);
			} else {
				ClientLobbyManager.remove(id);
			}
		});
		ctx.get().setPacketHandled(true);
	}

	static final class Update {
		final String name;
		final List<ClientQueuedGame> queue;
		@Nullable
		final ClientGameDefinition currentGame;

		Update(String name, List<ClientQueuedGame> queue, @Nullable ClientGameDefinition currentGame) {
			this.name = name;
			this.queue = queue;
			this.currentGame = currentGame;
		}

		static Update decode(PacketBuffer buffer) {
			String name = buffer.readString(200);

			int queueSize = buffer.readVarInt();
			List<ClientQueuedGame> queue = new ArrayList<>(queueSize);
			for (int i = 0; i < queueSize; i++) {
				queue.add(ClientQueuedGame.decode(buffer));
			}

			ClientGameDefinition currentGame = null;
			if (buffer.readBoolean()) {
				currentGame = ClientGameDefinition.decode(buffer);
			}

			return new Update(name, queue, currentGame);
		}

		void encode(PacketBuffer buffer) {
			buffer.writeString(name, 200);

			buffer.writeVarInt(queue.size());
			for (ClientQueuedGame game : queue) {
				game.encode(buffer);
			}

			buffer.writeBoolean(currentGame != null);
			if (currentGame != null) {
				currentGame.encode(buffer);
			}
		}
	}
}
