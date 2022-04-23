package com.lovetropics.minigames.client.screen;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.LanguageMap;
import net.minecraft.util.text.StringTextComponent;

public final class TrimmedText {
	private final ITextComponent text;

	private IReorderingProcessor trimmedText;
	private int trimmedWidth = -1;

	private TrimmedText(ITextComponent text) {
		this.text = text;
	}

	public static TrimmedText of(ITextComponent text) {
		return new TrimmedText(text);
	}

	public static TrimmedText of(String text) {
		return new TrimmedText(new StringTextComponent(text));
	}

	public ITextComponent text() {
		return this.text;
	}

	public IReorderingProcessor forWidth(FontRenderer font, int width) {
		IReorderingProcessor trimmed = this.trimmedText;
		if (trimmed == null || width != this.trimmedWidth) {
			this.trimmedText = trimmed = this.computeForWidth(font, width);
			this.trimmedWidth = width;
		}
		return trimmed;
	}

	public boolean isTrimmedForWidth(FontRenderer font, int width) {
		return font.width(this.text) > width;
	}

	private IReorderingProcessor computeForWidth(FontRenderer font, int width) {
		return LanguageMap.getInstance().getVisualOrder(font.substrByWidth(this.text, width));
	}
}
