package com.lovetropics.minigames.common.core.game.player;

import net.minecraft.network.IPacket;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public interface PlayerOps {
	PlayerOps EMPTY = new PlayerOps() {
		@Override
		public void sendMessage(ITextComponent message, boolean actionBar) {
		}

		@Override
		public void addPotionEffect(EffectInstance effect) {
		}

		@Override
		public void playSound(SoundEvent sound, SoundCategory category, float volume, float pitch) {
		}

		@Override
		public void sendPacket(IPacket<?> packet) {
		}

		@Override
		public void sendPacket(SimpleChannel channel, Object message) {
		}
	};

	default void sendMessage(ITextComponent message) {
		this.sendMessage(message, false);
	}

	void sendMessage(ITextComponent message, boolean actionBar);

	void addPotionEffect(EffectInstance effect);

	void playSound(SoundEvent sound, SoundCategory category, float volume, float pitch);

	void sendPacket(IPacket<?> packet);

	void sendPacket(SimpleChannel channel, Object message);
}
