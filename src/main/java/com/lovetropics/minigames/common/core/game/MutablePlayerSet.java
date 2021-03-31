package com.lovetropics.minigames.common.core.game;

import com.google.common.collect.AbstractIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

public final class MutablePlayerSet implements PlayerSet {
	private final MinecraftServer server;
	private final Set<UUID> players = new ObjectOpenHashSet<>();

	public MutablePlayerSet(MinecraftServer server) {
		this.server = server;
	}

	public void clear() {
		this.players.clear();
	}

	public boolean add(ServerPlayerEntity player) {
		return this.players.add(player.getUniqueID());
	}

	public boolean remove(UUID id) {
		return this.players.remove(id);
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
	public ServerPlayerEntity getPlayerBy(UUID id) {
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
