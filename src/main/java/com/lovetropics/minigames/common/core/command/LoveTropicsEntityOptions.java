package com.lovetropics.minigames.common.core.command;

import com.mojang.brigadier.StringReader;
import net.minecraft.command.arguments.EntityOptions;
import net.minecraft.util.text.StringTextComponent;

public final class LoveTropicsEntityOptions {
	public static void register() {
		EntityOptions.register(
				"current_world",
				parser -> {
					StringReader reader = parser.getReader();
					boolean enable = reader.readBoolean();
					if (enable) {
						parser.setCurrentWorldOnly();
					}
				},
				parser -> true,
				new StringTextComponent("Filters the search to only apply to the source world.")
		);
	}
}
