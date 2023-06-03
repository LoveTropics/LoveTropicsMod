package com.lovetropics.minigames.client.screen;

import net.minecraft.client.gui.Font;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.Component;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;

public final class TrimmedText {
	private final Component text;

	private FormattedCharSequence trimmedText;
	private int trimmedWidth = -1;

	private TrimmedText(Component text) {
		this.text = text;
	}

	public static TrimmedText of(Component text) {
		return new TrimmedText(text);
	}

	public static TrimmedText of(String text) {
		return new TrimmedText(Component.literal(text));
	}

	public Component text() {
		return this.text;
	}

	public FormattedCharSequence forWidth(Font font, int width) {
		FormattedCharSequence trimmed = this.trimmedText;
		if (trimmed == null || width != this.trimmedWidth) {
			this.trimmedText = trimmed = this.computeForWidth(font, width);
			this.trimmedWidth = width;
		}
		return trimmed;
	}

	public boolean isTrimmedForWidth(Font font, int width) {
		return font.width(this.text) > width;
	}

	private FormattedCharSequence computeForWidth(Font font, int width) {
		return Language.getInstance().getVisualOrder(font.substrByWidth(this.text, width));
	}
}
