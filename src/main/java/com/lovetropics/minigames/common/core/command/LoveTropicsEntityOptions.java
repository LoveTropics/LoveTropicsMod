package com.lovetropics.minigames.common.core.command;

import com.mojang.brigadier.StringReader;
import net.minecraft.commands.arguments.selector.options.EntitySelectorOptions;
import net.minecraft.network.chat.TextComponent;

public final class LoveTropicsEntityOptions {
	public static void register() {
		EntitySelectorOptions.register(
				"current_world",
				parser -> {
					StringReader reader = parser.getReader();
					boolean enable = reader.readBoolean();
					if (enable) {
						parser.setWorldLimited();
					}
				},
				parser -> true,
				new TextComponent("Filters the search to only apply to the source world.")
		);
	}
}
