package com.lovetropics.minigames.common.core.command.argument;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.Dimension;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;

public final class DimensionArgument {
	public static final DynamicCommandExceptionType DIMENSION_NOT_FOUND = new DynamicCommandExceptionType(arg ->
			new StringTextComponent("Dimension does not exist with id: " + arg)
	);

    public static RequiredArgumentBuilder<CommandSource, ResourceLocation> argument(String name) {
        return Commands.argument(name, ResourceLocationArgument.id())
                .suggests((context, builder) -> {
                    CommandSource source = context.getSource();
                    DimensionGeneratorSettings generatorSettings = source.getServer().getWorldData().worldGenSettings();
                    SimpleRegistry<Dimension> dimensions = generatorSettings.dimensions();
                    return ISuggestionProvider.suggestResource(
                            dimensions.keySet().stream(),
                            builder
                    );
                });
    }

	public static Dimension get(CommandContext<CommandSource> context, String name) throws CommandSyntaxException {
		ResourceLocation key = ResourceLocationArgument.getId(context, name);

		CommandSource source = context.getSource();
		DimensionGeneratorSettings generatorSettings = source.getServer().getWorldData().worldGenSettings();
		SimpleRegistry<Dimension> dimensions = generatorSettings.dimensions();

		Dimension dimension = dimensions.get(key);
		if (dimension == null) {
			throw DIMENSION_NOT_FOUND.create(key);
		}

		return dimension;
	}
}
