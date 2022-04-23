package com.lovetropics.minigames.common.core.command.argument;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.MappedRegistry;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldGenSettings;

public final class DimensionArgument {
	public static final DynamicCommandExceptionType DIMENSION_NOT_FOUND = new DynamicCommandExceptionType(arg ->
			new TextComponent("Dimension does not exist with id: " + arg)
	);

    public static RequiredArgumentBuilder<CommandSourceStack, ResourceLocation> argument(String name) {
        return Commands.argument(name, ResourceLocationArgument.id())
                .suggests((context, builder) -> {
                    CommandSourceStack source = context.getSource();
                    WorldGenSettings generatorSettings = source.getServer().getWorldData().worldGenSettings();
                    MappedRegistry<LevelStem> dimensions = generatorSettings.dimensions();
                    return SharedSuggestionProvider.suggestResource(
                            dimensions.keySet().stream(),
                            builder
                    );
                });
    }

	public static LevelStem get(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
		ResourceLocation key = ResourceLocationArgument.getId(context, name);

		CommandSourceStack source = context.getSource();
		WorldGenSettings generatorSettings = source.getServer().getWorldData().worldGenSettings();
		MappedRegistry<LevelStem> dimensions = generatorSettings.dimensions();

		LevelStem dimension = dimensions.get(key);
		if (dimension == null) {
			throw DIMENSION_NOT_FOUND.create(key);
		}

		return dimension;
	}
}
