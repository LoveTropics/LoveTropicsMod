package com.lovetropics.minigames.client.lobby.manage.state;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;
import it.unimi.dsi.fastutil.ints.*;
import net.minecraft.network.PacketBuffer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;

public final class ClientLobbyQueue implements Iterable<ClientLobbyQueuedGame> {
	private final IntList queue = new IntArrayList();
	private final Int2ObjectMap<ClientLobbyQueuedGame> games = new Int2ObjectOpenHashMap<>();

	public void add(int id, ClientLobbyQueuedGame game) {
		this.queue.add(id);
		this.games.put(id, game);
	}

	public void update(int id, ClientLobbyQueuedGame game) {
		this.games.replace(id, game);
	}

	public void applyUpdates(IntList queue, Int2ObjectMap<ClientLobbyQueuedGame> updated) {
		this.games.keySet().removeIf((IntPredicate) id -> !queue.contains(id));
		this.games.putAll(updated);

		this.queue.clear();
		this.queue.addAll(queue);
	}

	@Nullable
	public ClientLobbyQueuedGame byId(int id) {
		return this.games.get(id);
	}

	@Nonnull
	private ClientLobbyQueuedGame byIdOrThrow(int key) {
		ClientLobbyQueuedGame game = games.get(key);
		return Objects.requireNonNull(game, "game in queue with missing definition");
	}

	public int size() {
		return this.queue.size();
	}

	public boolean isEmpty() {
		return queue.isEmpty();
	}

	@Override
	public Iterator<ClientLobbyQueuedGame> iterator() {
		return Iterators.transform(entries().iterator(), Entry::game);
	}

	public Iterable<Entry> entries() {
		return () -> {
			IntListIterator iterator = queue.iterator();
			return new AbstractIterator<Entry>() {
				@Override
				protected Entry computeNext() {
					if (!iterator.hasNext()) {
						return endOfData();
					}
					int id = iterator.nextInt();
					ClientLobbyQueuedGame game = byIdOrThrow(id);
					return new Entry(id, game);
				}
			};
		};
	}

	public void encode(PacketBuffer buffer) {
		buffer.writeVarInt(queue.size());

		queue.forEach((IntConsumer) id -> {
			ClientLobbyQueuedGame game = byIdOrThrow(id);
			buffer.writeVarInt(id);
			game.encode(buffer);
		});
	}

	public static ClientLobbyQueue decode(PacketBuffer buffer) {
		ClientLobbyQueue queue = new ClientLobbyQueue();

		int queueSize = buffer.readVarInt();
		for (int i = 0; i < queueSize; i++) {
			int id = buffer.readVarInt();
			ClientLobbyQueuedGame game = ClientLobbyQueuedGame.decode(buffer);
			queue.add(id, game);
		}

		return queue;
	}

	public static final class Entry {
		private final int id;
		private final ClientLobbyQueuedGame game;

		Entry(int id, ClientLobbyQueuedGame game) {
			this.id = id;
			this.game = game;
		}

		public int id() {
			return id;
		}

		public ClientLobbyQueuedGame game() {
			return game;
		}
	}
}
