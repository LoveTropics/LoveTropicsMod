package com.lovetropics.minigames.common.core.game.player;

import com.google.common.base.Predicate;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.potion.EffectInstance;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.Iterator;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface PlayerIterable extends PlayerOps, Iterable<ServerPlayerEntity> {
	default PlayerIterable filter(Predicate<? super ServerPlayerEntity> predicate) {
		return () -> Iterators.filter(this.iterator(), predicate);
	}

	@Override
	Iterator<ServerPlayerEntity> iterator();

	default Stream<ServerPlayerEntity> stream() {
		return StreamSupport.stream(spliterator(), false);
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

	static Iterator<ServerPlayerEntity> resolvingIterator(MinecraftServer server, Iterator<UUID> ids) {
		PlayerList playerList = server.getPlayerList();
		return new AbstractIterator<ServerPlayerEntity>() {
			@Override
			protected ServerPlayerEntity computeNext() {
				while (true) {
					if (!ids.hasNext()) {
						return endOfData();
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