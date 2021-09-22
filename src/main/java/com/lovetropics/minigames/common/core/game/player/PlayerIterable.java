package com.lovetropics.minigames.common.core.game.player;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface PlayerIterable extends PlayerOps, Iterable<ServerPlayerEntity> {
	static PlayerIterable from(Iterable<ServerPlayerEntity> iterable) {
		return iterable::iterator;
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
}
