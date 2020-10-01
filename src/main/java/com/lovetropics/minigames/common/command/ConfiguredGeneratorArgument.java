package com.lovetropics.minigames.common.command;

import com.lovetropics.minigames.common.map.generator.ConfiguredGenerator;
import com.lovetropics.minigames.common.map.generator.ConfiguredGenerators;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

public final class ConfiguredGeneratorArgument {
	private static final DynamicCommandExceptionType DOES_NOT_EXIST = new DynamicCommandExceptionType(id -> {
		return new StringTextComponent("Generator does not exist with id '" + id + "'");
	});

	public static RequiredArgumentBuilder<CommandSource, ResourceLocation> argument(String name) {
		return Commands.argument(name, ResourceLocationArgument.resourceLocation())
				.suggests((context, builder) -> {
					return ISuggestionProvider.func_212476_a(
							ConfiguredGenerators.getKeys().stream(),
							builder
					);
				});
	}

	public static ConfiguredGenerator get(CommandContext<CommandSource> context, String name) throws CommandSyntaxException {
		ResourceLocation key = ResourceLocationArgument.getResourceLocation(context, name);
		ConfiguredGenerator generator = ConfiguredGenerators.get(key);
		if (generator == null) {
			throw DOES_NOT_EXIST.create(key);
		}

		return generator;
	}
}
