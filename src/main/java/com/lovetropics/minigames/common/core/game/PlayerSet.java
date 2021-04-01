package com.lovetropics.minigames.common.core.game;

import com.lovetropics.minigames.common.core.game.statistics.PlayerKey;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.potion.EffectInstance;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Iterator;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface PlayerSet extends PlayerOps, Iterable<ServerPlayerEntity> {
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
	default void sendMessage(ITextComponent message, boolean actionBar) {
		for (ServerPlayerEntity player : this) {
			player.sendStatusMessage(message, actionBar);
		}
	}

	@Override
	default void addPotionEffect(EffectInstance effect) {
		for (ServerPlayerEntity player : this) {
			player.addPotionEffect(effect);
		}
	}

	@Override
	default void sendPacket(IPacket<?> packet) {
		for (ServerPlayerEntity player : this) {
			player.connection.sendPacket(packet);
		}
	}

	@Override
	default void sendPacket(SimpleChannel channel, Object message) {
		for (ServerPlayerEntity player : this) {
			channel.send(PacketDistributor.PLAYER.with(() -> player), message);
		}
	}

	default Stream<ServerPlayerEntity> stream() {
		return StreamSupport.stream(spliterator(), false);
	}
}
