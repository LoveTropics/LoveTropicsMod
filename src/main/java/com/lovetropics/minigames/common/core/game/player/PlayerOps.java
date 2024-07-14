package com.lovetropics.minigames.common.core.game.player;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;

public interface PlayerOps {
	PlayerOps EMPTY = new PlayerOps() {
		@Override
		public void sendMessage(Component message, boolean actionBar) {
		}

		@Override
		public void addPotionEffect(MobEffectInstance effect) {
		}

		@Override
		public void playSound(SoundEvent sound, SoundSource category, float volume, float pitch) {
		}

		@Override
		public void sendPacket(Packet<?> packet) {
		}

		@Override
		public void sendPacket(CustomPacketPayload message) {
		}
	};

	default void sendMessage(Component message) {
		sendMessage(message, false);
	}

	void sendMessage(Component message, boolean actionBar);

	void addPotionEffect(MobEffectInstance effect);

	void playSound(SoundEvent sound, SoundSource category, float volume, float pitch);

	void sendPacket(Packet<?> packet);

	void sendPacket(CustomPacketPayload message);
}
