package com.lovetropics.minigames.common.core.command.argument;

import com.lovetropics.minigames.common.core.game.IGameDefinition;
import com.lovetropics.minigames.common.core.game.config.GameConfig;
import com.lovetropics.minigames.common.core.game.config.GameConfigs;
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

public final class GameConfigArgument {
	public static final DynamicCommandExceptionType GAME_CONFIG_NOT_FOUND = new DynamicCommandExceptionType(arg ->
			new StringTextComponent("Game config does not exist with id: " + arg)
	);

    public static RequiredArgumentBuilder<CommandSource, ResourceLocation> argument(String name) {
        return Commands.argument(name, ResourceLocationArgument.resourceLocation())
                .suggests((context, builder) -> {
                    return ISuggestionProvider.func_212476_a(
							GameConfigs.REGISTRY.stream().map(IGameDefinition::getId),
                            builder
                    );
                });
    }

	public static GameConfig get(CommandContext<CommandSource> context, String name) throws CommandSyntaxException {
		ResourceLocation id = ResourceLocationArgument.getResourceLocation(context, name);

		GameConfig config = GameConfigs.REGISTRY.get(id);
		if (config == null) {
			throw GAME_CONFIG_NOT_FOUND.create(id);
		}

		return config;
	}
}
