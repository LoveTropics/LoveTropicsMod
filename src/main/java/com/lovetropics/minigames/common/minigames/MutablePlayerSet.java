package com.lovetropics.minigames.common.minigames;

import com.google.common.collect.AbstractIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class MutablePlayerSet implements PlayerSet {
	private final MinecraftServer server;
	private final Set<UUID> players = new ObjectOpenHashSet<>();

	private final List<Listeners> listeners = new ArrayList<>();

	public MutablePlayerSet(MinecraftServer server) {
		this.server = server;
	}

	@Override
	public void addListener(Listeners listeners) {
		this.listeners.add(listeners);
	}

	@Override
	public void removeListener(Listeners listeners) {
		this.listeners.remove(listeners);
	}

	public void clear() {
		for (UUID id : this.players) {
			ServerPlayerEntity player = this.server.getPlayerList().getPlayerByUUID(id);
			for (Listeners listener : this.listeners) {
				listener.onRemovePlayer(id, player);
			}
		}
		this.players.clear();
	}

	public boolean add(ServerPlayerEntity player) {
		if (this.players.add(player.getUniqueID())) {
			for (Listeners listener : this.listeners) {
				listener.onAddPlayer(player);
			}
			return true;
		}
		return false;
	}

	public boolean remove(UUID id) {
		if (this.players.remove(id)) {
			ServerPlayerEntity player = this.server.getPlayerList().getPlayerByUUID(id);
			for (Listeners listener : this.listeners) {
				listener.onRemovePlayer(id, player);
			}
			return true;
		}
		return false;
	}

	public boolean remove(Entity entity) {
		return this.remove(entity.getUniqueID());
	}

	@Override
	public boolean contains(UUID id) {
		return this.players.contains(id);
	}

	@Nullable
	@Override
	public ServerPlayerEntity getPlayerById(UUID id) {
		return this.players.contains(id) ? this.server.getPlayerList().getPlayerByUUID(id) : null;
	}

	@Override
	public int size() {
		return this.players.size();
	}

	@Override
	public boolean isEmpty() {
		return this.players.isEmpty();
	}

	@Override
	public void sendMessage(ITextComponent message) {
		this.sendMessage(message, false);
	}

	@Override
	public void sendMessage(ITextComponent message, boolean actionBar) {
		for (ServerPlayerEntity player : this) {
			player.sendStatusMessage(message, actionBar);
		}
	}

	@Override
	public void addPotionEffect(EffectInstance effect) {
		for (ServerPlayerEntity player : this) {
			player.addPotionEffect(effect);
		}
	}

	@Override
	public Iterator<ServerPlayerEntity> iterator() {
		PlayerList playerList = this.server.getPlayerList();
		Iterator<UUID> ids = this.players.iterator();

		return new AbstractIterator<ServerPlayerEntity>() {
			@Override
			protected ServerPlayerEntity computeNext() {
				while (true) {
					if (!ids.hasNext()) {
						return this.endOfData();
					}

					UUID id = ids.next();
					ServerPlayerEntity player = playerList.getPlayerByUUID(id);
					if (player != null) {
						return player;
					}
				}
			}
		};
	}
}
