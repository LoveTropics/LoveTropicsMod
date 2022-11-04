package com.lovetropics.minigames.common.core.game.util;

import com.lovetropics.lib.codec.MoreCodecs;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Unit;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record TemplatedText(Component template) {
	public static final Codec<TemplatedText> CODEC = MoreCodecs.TEXT.xmap(TemplatedText::new, TemplatedText::template);

	private static final Pattern PATTERN = Pattern.compile("%([a-zA-Z0-9_]+)%");

	public Component apply(Map<String, Component> values) {
		if (values.isEmpty()) {
			return template;
		}

		MutableComponent result = new TextComponent("");
		template.visit((FormattedText.StyledContentConsumer<Unit>) (style, text) -> {
			Matcher matcher = PATTERN.matcher(text);
			while (matcher.find()) {
				String group = matcher.group();
				Component value = values.get(group.substring(1, group.length() - 1));
				if (value != null) {
					if (matcher.start() > 0) {
						result.append(new TextComponent(text.substring(0, matcher.start())).withStyle(style));
					}
					Style mergedStyle = value.getStyle().applyTo(style);
					result.append(value.copy().setStyle(mergedStyle));
					text = text.substring(matcher.end());
				}
			}
			if (!text.isEmpty()) {
				result.append(new TextComponent(text).withStyle(style));
			}
			return Optional.empty();
		}, Style.EMPTY);
		return result;
	}
}
