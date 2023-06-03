package com.lovetropics.minigames.common.core.game.util;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionContext;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionParameter;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Unit;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record TemplatedText(Component template) {
	public static final Codec<TemplatedText> CODEC = MoreCodecs.TEXT.xmap(TemplatedText::new, TemplatedText::template);

	private static final Pattern PATTERN = Pattern.compile("%([a-zA-Z0-9_]+)%");

	public Component apply(GameActionContext context) {
		Map<String, Component> values = new Object2ObjectArrayMap<>();
		context.get(GameActionParameter.PACKAGE_SENDER).ifPresent(name -> values.put("sender", Component.literal(name)));
		context.get(GameActionParameter.KILLER).ifPresent(player -> values.put("killer", player.getDisplayName()));
		context.get(GameActionParameter.KILLED).ifPresent(player -> values.put("killed", player.getDisplayName()));
		return apply(values);
	}

	public Component apply(Map<String, Component> values) {
		if (values.isEmpty()) {
			return template;
		}

		MutableComponent result = Component.literal("");
		template.visit((FormattedText.StyledContentConsumer<Unit>) (style, text) -> {
			Matcher matcher = PATTERN.matcher(text);
			while (matcher.find()) {
				String group = matcher.group();
				Component value = values.get(group.substring(1, group.length() - 1));
				if (value != null) {
					if (matcher.start() > 0) {
						result.append(Component.literal(text.substring(0, matcher.start())).withStyle(style));
					}
					Style mergedStyle = value.getStyle().applyTo(style);
					result.append(value.copy().setStyle(mergedStyle));
					text = text.substring(matcher.end());
				}
			}
			if (!text.isEmpty()) {
				result.append(Component.literal(text).withStyle(style));
			}
			return Optional.empty();
		}, Style.EMPTY);
		return result;
	}
}
