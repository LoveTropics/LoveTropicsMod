package com.lovetropics.minigames.common.core.game.util;

import com.lovetropics.lib.codec.MoreCodecs;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.function.Function;

public record TemplatedText(String text, ChatFormatting style) {
	private static final Codec<TemplatedText> INLINE_CODEC = Codec.STRING.xmap(s -> new TemplatedText(s, null), t -> t.text);
	private static final Codec<TemplatedText> STYLED_CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.STRING.fieldOf("text").forGetter(c -> c.text),
			MoreCodecs.FORMATTING.fieldOf("color").forGetter(c -> c.style)
	).apply(i, TemplatedText::new));

	public static final Codec<TemplatedText> CODEC = Codec.either(INLINE_CODEC, STYLED_CODEC).xmap(
			either -> either.map(Function.identity(), Function.identity()),
			text -> text.style != null ? Either.right(text) : Either.left(text)
	);

	public Component apply(Object... variables) {
		return new TranslatableComponent(text, variables).withStyle(this.style);
	}
}
