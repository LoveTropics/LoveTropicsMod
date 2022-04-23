package com.lovetropics.minigames.common.core.game.util;

import com.lovetropics.lib.codec.MoreCodecs;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;

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
	private final ChatFormatting style;

	public TemplatedText(String text, ChatFormatting style) {
		this.text = text;
		this.style = style;
	}

	public Component apply(Object... variables) {
		return new TranslatableComponent(text, variables).withStyle(this.style);
	}
}
