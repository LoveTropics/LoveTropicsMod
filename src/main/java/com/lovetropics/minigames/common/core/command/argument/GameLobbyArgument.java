package com.lovetropics.minigames.common.core.command.argument;

import com.lovetropics.minigames.common.core.game.IGameManager;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.TextComponent;

public final class GameLobbyArgument {
	public static final DynamicCommandExceptionType GAME_LOBBY_NOT_FOUND = new DynamicCommandExceptionType(arg ->
			new TextComponent("Could not find lobby with name: " + arg)
	);

	public static RequiredArgumentBuilder<CommandSourceStack, String> argument(String name) {
		return Commands.argument(name, StringArgumentType.string())
				.suggests((context, builder) -> {
					return SharedSuggestionProvider.suggest(
							IGameManager.get().getVisibleLobbies(context.getSource()).map(lobby -> lobby.getMetadata().commandId()),
							builder
					);
				});
	}

	public static IGameLobby get(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
		String id = StringArgumentType.getString(context, name);

		IGameLobby lobby = IGameManager.get().getLobbyByCommandId(id);
		if (lobby == null || !lobby.isVisibleTo(context.getSource())) {
			throw GAME_LOBBY_NOT_FOUND.create(id);
		}

		return lobby;
	}
}
