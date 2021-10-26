package com.lovetropics.minigames.common.core.game.player;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;

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
	public Iterator<ServerPlayerEntity> iterator() {
		return PlayerIterable.resolvingIterator(this.server, this.players.iterator());
	}
}
