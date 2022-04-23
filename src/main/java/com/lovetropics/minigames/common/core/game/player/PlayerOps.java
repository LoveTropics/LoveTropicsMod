package com.lovetropics.minigames.common.core.game.player;

import net.minecraft.network.protocol.Packet;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.network.simple.SimpleChannel;

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
		public void sendPacket(SimpleChannel channel, Object message) {
		}
	};

	default void sendMessage(Component message) {
		this.sendMessage(message, false);
	}

	void sendMessage(Component message, boolean actionBar);

	void addPotionEffect(MobEffectInstance effect);

	void playSound(SoundEvent sound, SoundSource category, float volume, float pitch);

	void sendPacket(Packet<?> packet);

	void sendPacket(SimpleChannel channel, Object message);
}
