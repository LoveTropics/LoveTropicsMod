package com.lovetropics.minigames.common.minigames;

import com.lovetropics.minigames.common.minigames.statistics.PlayerKey;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Iterator;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface PlayerSet extends Iterable<ServerPlayerEntity> {
	PlayerSet EMPTY = new PlayerSet() {
		@Override
		public void addListener(Listeners listeners) {
		}

		@Override
		public void removeListener(Listeners listeners) {
		}

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

	void addListener(Listeners listeners);

	void removeListener(Listeners listeners);

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

	boolean isEmpty();

	default void sendMessage(ITextComponent message) {
		this.sendMessage(message, false);
	}

	default void sendMessage(ITextComponent message, boolean actionBar) {
		for (ServerPlayerEntity player : this) {
			player.sendStatusMessage(message, actionBar);
		}
	}

	default void addPotionEffect(EffectInstance effect) {
		for (ServerPlayerEntity player : this) {
			player.addPotionEffect(effect);
		}
	}

	default void sendPacket(IPacket<?> packet) {
		for (ServerPlayerEntity player : this) {
			player.connection.sendPacket(packet);
		}
	}

	default void sendPacket(SimpleChannel channel, Object message) {
		for (ServerPlayerEntity player : this) {
			channel.send(PacketDistributor.PLAYER.with(() -> player), message);
		}
	}

	default Stream<ServerPlayerEntity> stream() {
		return StreamSupport.stream(spliterator(), false);
	}

	interface Listeners {
		default void onAddPlayer(ServerPlayerEntity player) {
		}

		default void onRemovePlayer(UUID id, @Nullable ServerPlayerEntity player) {
		}
	}
}
