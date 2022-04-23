package com.lovetropics.minigames.common.core.game;

import net.minecraft.network.chat.Component;

public class GameException extends RuntimeException {
	private final Component message;

	public GameException(Component message) {
		super(message.getString());
		this.message = message;
	}

	public GameException(Component message, Throwable cause) {
		super(message.getString(), cause);
		this.message = message;
	}

	public Component getTextMessage() {
		return this.message;
	}
}
