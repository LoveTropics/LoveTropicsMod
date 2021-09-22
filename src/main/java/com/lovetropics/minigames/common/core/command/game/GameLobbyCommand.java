package com.lovetropics.minigames.common.core.command.game;

import com.lovetropics.minigames.client.lobby.ManageLobbyMessage;
import com.lovetropics.minigames.common.core.game.GameResult;
import com.lovetropics.minigames.common.core.game.impl.MultiGameManager;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.PacketDistributor;

import static net.minecraft.command.Commands.literal;

public class GameLobbyCommand {
	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		// @formatter:off
        dispatcher.register(
            literal("game").then(literal("lobby")
                .then(literal("create")
                    .executes(GameLobbyCommand::createLobby)
                )
            )
        );
        // @formatter:on
	}

	private static int createLobby(CommandContext<CommandSource> context) throws CommandSyntaxException {
		// TODO: name the lobby here?
		ServerPlayerEntity player = context.getSource().asPlayer();

		GameResult<IGameLobby> result = MultiGameManager.INSTANCE.createGameLobby("Lobby", player);
		if (result.isError()) {
			context.getSource().sendErrorMessage(result.getError());
			return Command.SINGLE_SUCCESS;
		}

		IGameLobby lobby = result.getOk();

		int networkId = lobby.getId().getNetworkId();
		LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ManageLobbyMessage(networkId));

		return Command.SINGLE_SUCCESS;
	}
}
