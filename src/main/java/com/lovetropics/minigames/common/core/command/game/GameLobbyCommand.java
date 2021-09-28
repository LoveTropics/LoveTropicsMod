package com.lovetropics.minigames.common.core.command.game;

import com.lovetropics.minigames.client.lobby.manage.ClientManageLobbyMessage;
import com.lovetropics.minigames.common.core.command.argument.GameConfigArgument;
import com.lovetropics.minigames.common.core.command.argument.GameLobbyArgument;
import com.lovetropics.minigames.common.core.game.GameResult;
import com.lovetropics.minigames.common.core.game.config.GameConfig;
import com.lovetropics.minigames.common.core.game.impl.MultiGameManager;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import com.lovetropics.minigames.common.core.game.lobby.ILobbyManagement;
import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.PacketDistributor;

import static net.minecraft.command.Commands.literal;

public class GameLobbyCommand {
	private static final SimpleCommandExceptionType NOT_LOBBY_INITIATOR = new SimpleCommandExceptionType(
			new StringTextComponent("You cannot manage this lobby because you are not the initiator!")
	);

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		// @formatter:off
        dispatcher.register(
            literal("game").then(literal("lobby")
				.requires(source -> source.hasPermissionLevel(2))
                .then(literal("create")
                    .executes(GameLobbyCommand::createLobby)
                )
				.then(literal("manage")
					.then(GameLobbyArgument.argument("lobby")
					.executes(GameLobbyCommand::manageLobby)
				))
				.then(literal("enqueue")
					.then(GameLobbyArgument.argument("lobby")
					.then(GameConfigArgument.argument("game")
					.executes(GameLobbyCommand::enqueueGame)
				)))
            )
        );
        // @formatter:on
	}

	private static int createLobby(CommandContext<CommandSource> context) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().asPlayer();

		String name = player.getScoreboardName() + "'s Lobby";

		GameResult<IGameLobby> result = MultiGameManager.INSTANCE.createGameLobby(name, player);
		if (result.isError()) {
			context.getSource().sendErrorMessage(result.getError());
			return Command.SINGLE_SUCCESS;
		}

		IGameLobby lobby = result.getOk();

		ClientManageLobbyMessage message = ClientManageLobbyMessage.open(lobby);
		LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);

		return Command.SINGLE_SUCCESS;
	}

	private static int manageLobby(CommandContext<CommandSource> context) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().asPlayer();
		IGameLobby lobby = GameLobbyArgument.get(context, "lobby");

		ILobbyManagement management = lobby.getManagement();
		if (management.startManaging(player)) {
			ClientManageLobbyMessage message = ClientManageLobbyMessage.open(lobby);
			LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
		} else {
			throw NOT_LOBBY_INITIATOR.create();
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
