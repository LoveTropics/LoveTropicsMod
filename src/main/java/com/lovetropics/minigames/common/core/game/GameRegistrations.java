package com.lovetropics.minigames.common.core.game;

import com.google.common.collect.Lists;
import com.lovetropics.minigames.common.core.game.player.PlayerIterable;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.player.PlayerSet;
import com.lovetropics.minigames.common.core.game.util.TeamAllocator;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class GameRegistrations implements PlayerSet {
	private final MinecraftServer server;

	private final Set<UUID> players = new ObjectOpenHashSet<>();
	private final Map<UUID, PlayerRole> rolePreferences = new Object2ObjectOpenHashMap<>();

	public GameRegistrations(MinecraftServer server) {
		this.server = server;
	}

	public void clear() {
		this.players.clear();
		this.rolePreferences.clear();
	}

	public TeamAllocator<PlayerRole, ServerPlayerEntity> createAllocator() {
		TeamAllocator<PlayerRole, ServerPlayerEntity> allocator = new TeamAllocator<>(Lists.newArrayList(PlayerRole.ROLES));
		allocator.setOverflowTeam(PlayerRole.SPECTATOR);

		for (ServerPlayerEntity player : this) {
			PlayerRole role = rolePreferences.get(player.getUniqueID());
			allocator.addPlayer(player, role);
		}

		return allocator;
	}

	public boolean add(UUID id, @Nullable PlayerRole requestedRole) {
		if (players.add(id)) {
			if (requestedRole != null) {
				rolePreferences.put(id, requestedRole);
			}
			return true;
		} else {
			return false;
		}
	}

	public boolean set(UUID id, @Nullable PlayerRole requestedRole) {
		if (players.contains(id)) {
			if (requestedRole != null) {
				return rolePreferences.put(id, requestedRole) != requestedRole;
			} else {
				return rolePreferences.remove(id) != null;
			}
		}
		return false;
	}

	public boolean remove(UUID id) {
		if (players.remove(id)) {
			rolePreferences.remove(id);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean contains(UUID id) {
		return players.contains(id);
	}

	@Override
	public int size() {
		return players.size();
	}

	@Nullable
	public PlayerRole getRoleFor(UUID id) {
		return contains(id) ? rolePreferences.getOrDefault(id, PlayerRole.PARTICIPANT) : null;
	}

	@Nullable
	@Override
	public ServerPlayerEntity getPlayerBy(UUID id) {
		return contains(id) ? server.getPlayerList().getPlayerByUUID(id) : null;
	}

	@Override
	public Iterator<ServerPlayerEntity> iterator() {
		return PlayerIterable.resolvingIterator(server, players.iterator());
	}
}
