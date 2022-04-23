package com.lovetropics.minigames.common.core.game.player;

import com.google.common.base.Predicate;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.NetworkManager;
import net.minecraft.potion.EffectInstance;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface PlayerIterable extends PlayerOps, Iterable<ServerPlayerEntity> {
	default PlayerIterable filter(Predicate<? super ServerPlayerEntity> predicate) {
		return () -> Iterators.filter(this.iterator(), predicate);
	}

	default PlayerIterable excluding(ServerPlayerEntity player) {
		return this.filter(target -> target != player);
	}

	@Override
	Iterator<ServerPlayerEntity> iterator();

	default Stream<ServerPlayerEntity> stream() {
		return StreamSupport.stream(spliterator(), false);
	}

	@Override
	default void sendMessage(ITextComponent message, boolean actionBar) {
		for (ServerPlayerEntity player : this) {
			player.displayClientMessage(message, actionBar);
		}
	}

	@Override
	default void addPotionEffect(EffectInstance effect) {
		for (ServerPlayerEntity player : this) {
			player.addEffect(new EffectInstance(effect));
		}
	}

	@Override
	default void playSound(SoundEvent sound, SoundCategory category, float volume, float pitch) {
		for (ServerPlayerEntity player : this) {
			player.playNotifySound(sound, category, volume, pitch);
		}
	}

	@Override
	default void sendPacket(IPacket<?> packet) {
		for (ServerPlayerEntity player : this) {
			player.connection.send(packet);
		}
	}

	@Override
	default void sendPacket(SimpleChannel channel, Object message) {
		PacketDistributor.PacketTarget target = PacketDistributor.NMLIST.with(() -> {
			List<NetworkManager> networkManagers = new ArrayList<>();
			for (ServerPlayerEntity player : this) {
				networkManagers.add(player.connection.connection);
			}
			return networkManagers;
		});

		channel.send(target, message);
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
					ServerPlayerEntity player = playerList.getPlayer(id);
					if (player != null) {
						return player;
					}
				}
			}
		};
	}
}
