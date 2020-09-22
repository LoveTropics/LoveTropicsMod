package com.lovetropics.minigames.common.minigames;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.text.ITextComponent;

import java.util.UUID;

public interface PlayerSet extends Iterable<ServerPlayerEntity> {
	void addListener(Listeners listeners);

	boolean contains(Entity entity);

	boolean contains(UUID id);

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

	interface Listeners {
		default void onAddPlayer(ServerPlayerEntity player) {
		}

		default void onRemovePlayer(UUID id) {
		}
	}
}
