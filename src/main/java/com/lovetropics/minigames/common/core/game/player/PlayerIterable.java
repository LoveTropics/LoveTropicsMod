package com.lovetropics.minigames.common.core.game.player;

import com.google.common.base.Predicate;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.Connection;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface PlayerIterable extends PlayerOps, Iterable<ServerPlayer> {
	default PlayerIterable filter(Predicate<? super ServerPlayer> predicate) {
		return () -> Iterators.filter(this.iterator(), predicate);
	}

	default PlayerIterable excluding(ServerPlayer player) {
		return this.filter(target -> target != player);
	}

	@Override
	Iterator<ServerPlayer> iterator();

	default Stream<ServerPlayer> stream() {
		return StreamSupport.stream(spliterator(), false);
	}

	@Override
	default void sendMessage(Component message, boolean actionBar) {
		for (ServerPlayer player : this) {
			player.displayClientMessage(message, actionBar);
		}
	}

	@Override
	default void addPotionEffect(MobEffectInstance effect) {
		for (ServerPlayer player : this) {
			player.addEffect(new MobEffectInstance(effect));
		}
	}

	@Override
	default void playSound(SoundEvent sound, SoundSource category, float volume, float pitch) {
		for (ServerPlayer player : this) {
			player.playNotifySound(sound, category, volume, pitch);
		}
	}

	@Override
	default void sendPacket(Packet<?> packet) {
		for (ServerPlayer player : this) {
			player.connection.send(packet);
		}
	}

	@Override
	default void sendPacket(SimpleChannel channel, Object message) {
		PacketDistributor.PacketTarget target = PacketDistributor.NMLIST.with(() -> {
			List<Connection> networkManagers = new ArrayList<>();
			for (ServerPlayer player : this) {
				networkManagers.add(player.connection.connection);
			}
			return networkManagers;
		});

		channel.send(target, message);
	}

	static Iterator<ServerPlayer> resolvingIterator(MinecraftServer server, Iterator<UUID> ids) {
		PlayerList playerList = server.getPlayerList();
		return new AbstractIterator<ServerPlayer>() {
			@Override
			protected ServerPlayer computeNext() {
				while (true) {
					if (!ids.hasNext()) {
						return endOfData();
					}

					UUID id = ids.next();
					ServerPlayer player = playerList.getPlayer(id);
					if (player != null) {
						return player;
					}
				}
			}
		};
	}
}
