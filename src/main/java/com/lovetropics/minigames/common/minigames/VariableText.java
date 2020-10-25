package com.lovetropics.minigames.common.minigames;

import com.mojang.datafixers.Dynamic;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Optional;

public final class VariableText {
	private final String text;
	private final Style style;

	public VariableText(String text, Style style) {
		this.text = text;
		this.style = style;
	}

	public static <T> VariableText parse(Dynamic<T> root) {
		Optional<String> inlineText = root.asString();
		if (inlineText.isPresent()) {
			return new VariableText(inlineText.get(), new Style());
		}

		String text = root.get("text").asString("");

		Style style = new Style();

		TextFormatting color = TextFormatting.getValueByName(root.get("color").asString(""));
		if (color != null) {
			style.setColor(color);
		}

		return new VariableText(text, style);
	}

	public ITextComponent apply(Object... variables) {
		return new TranslationTextComponent(text, variables)
				.setStyle(style.createShallowCopy());
	}
}
