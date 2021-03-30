package com.lovetropics.minigames.common.core.game;

import net.minecraft.util.text.ITextComponent;

public class GameException extends RuntimeException {
	private final ITextComponent message;

	public GameException(ITextComponent message) {
		super(message.getString());
		this.message = message;
	}

	public GameException(ITextComponent message, Throwable cause) {
		super(message.getString(), cause);
		this.message = message;
	}

	public ITextComponent getTextMessage() {
		return this.message;
	}
}
