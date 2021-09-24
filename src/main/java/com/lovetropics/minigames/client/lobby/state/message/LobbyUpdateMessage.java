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
		ClientGameDefinition definition = lobby.getActiveGame() != null ? ClientGameDefinition.from(lobby.getActiveGame().getDefinition()) : null;
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
				ClientLobbyManager.addOrUpdate(id, update.name, update.queue, update.activeGame);
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
		final ClientGameDefinition activeGame;

		Update(String name, List<ClientQueuedGame> queue, @Nullable ClientGameDefinition activeGame) {
			this.name = name;
			this.queue = queue;
			this.activeGame = activeGame;
		}

		static Update decode(PacketBuffer buffer) {
			String name = buffer.readString(200);

			int queueSize = buffer.readVarInt();
			List<ClientQueuedGame> queue = new ArrayList<>(queueSize);
			for (int i = 0; i < queueSize; i++) {
				queue.add(ClientQueuedGame.decode(buffer));
			}

			ClientGameDefinition activeGame = null;
			if (buffer.readBoolean()) {
				activeGame = ClientGameDefinition.decode(buffer);
			}

			return new Update(name, queue, activeGame);
		}

		void encode(PacketBuffer buffer) {
			buffer.writeString(name, 200);

			buffer.writeVarInt(queue.size());
			for (ClientQueuedGame game : queue) {
				game.encode(buffer);
			}

			buffer.writeBoolean(activeGame != null);
			if (activeGame != null) {
				activeGame.encode(buffer);
			}
		}
	}
}
