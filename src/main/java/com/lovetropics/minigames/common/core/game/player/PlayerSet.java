package com.lovetropics.minigames.common.core.game.player;

import com.lovetropics.minigames.common.core.game.statistics.PlayerKey;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Iterator;
import java.util.UUID;

public interface PlayerSet extends PlayerIterable {
	PlayerSet EMPTY = new PlayerSet() {
		@Override
		public boolean contains(UUID id) {
			return false;
		}

		@Nullable
		@Override
		public ServerPlayerEntity getPlayerBy(UUID id) {
			return null;
		}

		@Override
		public int size() {
			return 0;
		}

		@Override
		public boolean isEmpty() {
			return true;
		}

		@Override
		public Iterator<ServerPlayerEntity> iterator() {
			return Collections.emptyIterator();
		}
	};

	static PlayerSet ofServer(MinecraftServer server) {
		return of(server.getPlayerList());
	}

	static PlayerSet of(PlayerList players) {
		return new PlayerSet() {
			@Override
			public boolean contains(UUID id) {
				return players.getPlayerByUUID(id) != null;
			}

			@Nullable
			@Override
			public ServerPlayerEntity getPlayerBy(UUID id) {
				return players.getPlayerByUUID(id);
			}

			@Override
			public int size() {
				return players.getCurrentPlayerCount();
			}

			@Override
			public Iterator<ServerPlayerEntity> iterator() {
				return players.getPlayers().iterator();
			}
		};
	}

	default boolean contains(Entity entity) {
		return this.contains(entity.getUniqueID());
	}

	boolean contains(UUID id);

	@Nullable
	ServerPlayerEntity getPlayerBy(UUID id);

	@Nullable
	default ServerPlayerEntity getPlayerBy(PlayerKey key) {
		return getPlayerBy(key.getId());
	}

	int size();

	default boolean isEmpty() {
		return size() == 0;
	}

	@Override
	Iterator<ServerPlayerEntity> iterator();
}
