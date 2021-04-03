package com.lovetropics.minigames.common.core.command.argument;

import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.IGameManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.text.StringTextComponent;

public final class GameInstanceArgument {
	public static final DynamicCommandExceptionType GAME_INSTANCE_NOT_FOUND = new DynamicCommandExceptionType(arg ->
			new StringTextComponent("Game instance does not exist with id: " + arg)
	);

    public static RequiredArgumentBuilder<CommandSource, String> argument(String name) {
        return Commands.argument(name, StringArgumentType.string())
                .suggests((context, builder) -> {
                    return ISuggestionProvider.suggest(
							IGameManager.get().getAllGames().stream().map(instance -> instance.getInstanceId().commandId),
                            builder
                    );
                });
    }

	public static IGameInstance get(CommandContext<CommandSource> context, String name) throws CommandSyntaxException {
		String id = StringArgumentType.getString(context, name);

		IGameInstance instance = IGameManager.get().getGameByCommandId(id);
		if (instance == null) {
			throw GAME_INSTANCE_NOT_FOUND.create(id);
		}

		return instance;
	}
}
