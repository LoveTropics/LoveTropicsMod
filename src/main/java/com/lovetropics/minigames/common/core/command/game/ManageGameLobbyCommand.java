package com.lovetropics.minigames.common.core.command.game;

import com.lovetropics.minigames.common.core.command.argument.GameConfigArgument;
import com.lovetropics.minigames.common.core.command.argument.GameLobbyArgument;
import com.lovetropics.minigames.common.core.game.GameResult;
import com.lovetropics.minigames.common.core.game.config.GameConfig;
import com.lovetropics.minigames.common.core.game.impl.MultiGameManager;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import com.lovetropics.minigames.common.core.game.util.GameTexts;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;

import static net.minecraft.command.Commands.literal;

public class ManageGameLobbyCommand {
	private static final SimpleCommandExceptionType NO_MANAGE_PERMISSION = new SimpleCommandExceptionType(GameTexts.Commands.noManagePermission());

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		// @formatter:off
        dispatcher.register(
            literal("game")
                .then(literal("create")
						.requires(source -> source.hasPermissionLevel(2))
						.executes(ManageGameLobbyCommand::createLobby)
                )
				.then(literal("manage")
					.requires(source -> source.hasPermissionLevel(2))
					.then(GameLobbyArgument.argument("lobby")
					.executes(ManageGameLobbyCommand::manageLobby)
				))
				.then(literal("enqueue")
					.requires(source -> source.hasPermissionLevel(2))
					.then(GameLobbyArgument.argument("lobby")
					.then(GameConfigArgument.argument("game")
					.executes(ManageGameLobbyCommand::enqueueGame)
				))
			)
        );
        // @formatter:on
	}

	private static int createLobby(CommandContext<CommandSource> context) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().asPlayer();
		String name = player.getScoreboardName() + "'s Lobby";

		GameResult<IGameLobby> result = MultiGameManager.INSTANCE.createGameLobby(name, player);
		if (result.isError()) {
			throw new SimpleCommandExceptionType(result.getError()).create();
		}

		IGameLobby lobby = result.getOk();
		lobby.getPlayers().join(player, null);

		lobby.getManagement().startManaging(player);

		return Command.SINGLE_SUCCESS;
	}

	private static int manageLobby(CommandContext<CommandSource> context) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().asPlayer();
		IGameLobby lobby = GameLobbyArgument.get(context, "lobby");

		if (!lobby.getManagement().startManaging(player)) {
			throw NO_MANAGE_PERMISSION.create();
		}

		return Command.SINGLE_SUCCESS;
	}

	private static int enqueueGame(CommandContext<CommandSource> context) throws CommandSyntaxException {
		IGameLobby lobby = GameLobbyArgument.get(context, "lobby");
		GameConfig game = GameConfigArgument.get(context, "game");

		lobby.getGameQueue().enqueue(game);

		return Command.SINGLE_SUCCESS;
	}
}
