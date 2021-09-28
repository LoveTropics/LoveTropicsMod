package com.lovetropics.minigames.client.lobby.manage.state.update;

import com.lovetropics.minigames.client.lobby.manage.ServerManageLobbyMessage;
import com.lovetropics.minigames.client.lobby.state.ClientGameDefinition;
import com.lovetropics.minigames.common.core.game.config.GameConfig;
import com.lovetropics.minigames.common.core.game.config.GameConfigs;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import com.lovetropics.minigames.common.util.PartialUpdate;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import java.util.function.Function;
import java.util.function.IntConsumer;

public abstract class ServerLobbyUpdate extends PartialUpdate<IGameLobby> {
	private static final Family<IGameLobby> FAMILY = Family.of(Type.values());

	public static final class Set extends AbstractSet<IGameLobby> {
		private Set() {
			super(FAMILY);
		}

		public static Set create() {
			return new Set();
		}

		public static Set decode(PacketBuffer buffer) {
			Set set = new Set();
			set.decodeSelf(buffer);
			return set;
		}

		public Set setName(String name) {
			this.add(new SetName(name));
			return this;
		}

		public Set enqueue(ClientGameDefinition definition) {
			this.add(new Enqueue(definition.id));
			return this;
		}

		public ServerManageLobbyMessage intoMessage(int id) {
			return new ServerManageLobbyMessage(id, this);
		}
	}

	public enum Type implements AbstractType<IGameLobby> {
		SET_NAME(SetName::decode),
		ENQUEUE(Enqueue::decode),
		UPDATE_QUEUE(UpdateQueue::decode);

		private final Function<PacketBuffer, ServerLobbyUpdate> decode;

		Type(Function<PacketBuffer, ServerLobbyUpdate> decode) {
			this.decode = decode;
		}

		@Override
		public ServerLobbyUpdate decode(PacketBuffer buffer) {
			return decode.apply(buffer);
		}
	}

	protected ServerLobbyUpdate(Type type) {
		super(type);
	}

	public static final class SetName extends ServerLobbyUpdate {
		private final String name;

		public SetName(String name) {
			super(Type.SET_NAME);
			this.name = name;
		}

		@Override
		public void applyTo(IGameLobby lobby) {
			// TODO
		}

		@Override
		protected void encode(PacketBuffer buffer) {
			buffer.writeString(name, 200);
		}

		static SetName decode(PacketBuffer buffer) {
			return new SetName(buffer.readString(200));
		}
	}

	public static final class Enqueue extends ServerLobbyUpdate {
		private final ResourceLocation definition;

		public Enqueue(ResourceLocation definition) {
			super(Type.ENQUEUE);
			this.definition = definition;
		}

		@Override
		public void applyTo(IGameLobby lobby) {
			GameConfig config = GameConfigs.REGISTRY.get(definition);
			if (config != null) {
				lobby.getGameQueue().enqueue(config);
				// TODO: packets & centralised packet tracking handling
			}
		}

		@Override
		protected void encode(PacketBuffer buffer) {
			buffer.writeResourceLocation(definition);
		}

		static Enqueue decode(PacketBuffer buffer) {
			return new Enqueue(buffer.readResourceLocation());
		}
	}

	public static final class UpdateQueue extends ServerLobbyUpdate {
		private final IntList queue;

		public UpdateQueue(IntList queue) {
			super(Type.UPDATE_QUEUE);
			this.queue = queue;
		}

		@Override
		public void applyTo(IGameLobby lobby) {
			// TODO
			// TODO: drop stale entries
		}

		@Override
		protected void encode(PacketBuffer buffer) {
			buffer.writeVarInt(queue.size());
			queue.forEach((IntConsumer) buffer::writeVarInt);
		}

		static UpdateQueue decode(PacketBuffer buffer) {
			int queueSize = buffer.readVarInt();
			IntList queue = new IntArrayList(queueSize);
			for (int i = 0; i < queueSize; i++) {
				queue.add(buffer.readVarInt());
			}

			return new UpdateQueue(queue);
		}
	}
}
