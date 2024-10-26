package com.lovetropics.minigames.client.screen;

import net.minecraft.client.gui.Font;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

import javax.annotation.Nullable;

public final class TrimmableText {
	private final Component text;

	@Nullable
	private FormattedCharSequence cachedTrimmed;
	private int cachedWidth = -1;

	private TrimmableText(Component text) {
		this.text = text;
	}

	public static TrimmableText of(Component text) {
		return new TrimmableText(text);
	}

	public static TrimmableText of(String text) {
		return new TrimmableText(Component.literal(text));
	}

	public Component text() {
		return text;
	}

	public FormattedCharSequence forWidth(Font font, int width) {
		FormattedCharSequence trimmed = cachedTrimmed;
		if (trimmed == null || width != cachedWidth) {
			cachedTrimmed = trimmed = computeForWidth(font, width);
			cachedWidth = width;
		}
		return trimmed;
	}

	public boolean isTrimmedForWidth(Font font, int width) {
		return font.width(text) > width;
	}

	private FormattedCharSequence computeForWidth(Font font, int width) {
		FormattedText trimmed = font.substrByWidth(text, width - font.width(CommonComponents.ELLIPSIS));
		if (text == trimmed) {
			return text.getVisualOrderText();
		}
		return Language.getInstance().getVisualOrder(FormattedText.composite(trimmed, CommonComponents.ELLIPSIS));
	}
}
