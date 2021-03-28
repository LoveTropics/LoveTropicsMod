package com.lovetropics.minigames.common.minigames;

import com.lovetropics.minigames.common.MoreCodecs;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.function.Function;

public final class TemplatedText {
	private static final Codec<TemplatedText> INLINE_CODEC = Codec.STRING.xmap(s -> new TemplatedText(s, null), t -> t.text);
	private static final Codec<TemplatedText> STYLED_CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.STRING.fieldOf("text").forGetter(c -> c.text),
				MoreCodecs.FORMATTING.fieldOf("color").forGetter(c -> c.style)
		).apply(instance, TemplatedText::new);
	});

	public static final Codec<TemplatedText> CODEC = Codec.either(INLINE_CODEC, STYLED_CODEC).xmap(
			either -> either.map(Function.identity(), Function.identity()),
			text -> text.style != null ? Either.right(text) : Either.left(text)
	);

	private final String text;
	private final TextFormatting style;

	public TemplatedText(String text, TextFormatting style) {
		this.text = text;
		this.style = style;
	}

	public ITextComponent apply(Object... variables) {
		return new TranslationTextComponent(text, variables).mergeStyle(this.style);
	}
}
