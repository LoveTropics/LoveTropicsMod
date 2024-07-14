package com.lovetropics.minigames.client.screen;

import net.minecraft.client.gui.Font;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import javax.annotation.Nullable;

public final class TrimmedText {
	public static final TrimmedText EMPTY = new TrimmedText(CommonComponents.EMPTY);

	private final Component text;

	@Nullable
	private FormattedCharSequence trimmedText;
	private int trimmedWidth = -1;

	private TrimmedText(Component text) {
		this.text = text;
	}

	public static TrimmedText of(Component text) {
		return new TrimmedText(text);
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
