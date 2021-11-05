package com.lovetropics.minigames.common.core.command.game;

import com.lovetropics.minigames.common.core.command.argument.GameLobbyArgument;
import com.lovetropics.minigames.common.core.command.argument.PlayerRoleArgument;
import com.lovetropics.minigames.common.core.game.GameResult;
import com.lovetropics.minigames.common.core.game.IGameManager;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobbyPlayers;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.util.GameTexts;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Unit;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static net.minecraft.command.Commands.literal;

public class JoinGameCommand {
	public static void register(final CommandDispatcher<CommandSource> dispatcher) {
		// @formatter:off
		dispatcher.register(
			literal("game")
				.then(joinBuilder("register"))
				.then(joinBuilder("join"))
				.then(joinBuilder("play"))
		);
		// @formatter:on
	}

	private static LiteralArgumentBuilder<CommandSource> joinBuilder(String name) {
		// @formatter:off
		return literal(name)
				.executes(ctx -> joinAsRole(ctx, null, null))
				.then(GameLobbyArgument.argument("lobby")
					.executes(ctx -> {
						IGameLobby lobby = GameLobbyArgument.get(ctx, "lobby");
						return joinAsRole(ctx, lobby, null);
					})
					.then(literal("as").requires(source -> source.hasPermissionLevel(2))
						.then(PlayerRoleArgument.argument("role")
						.executes(ctx -> {
							IGameLobby lobby = GameLobbyArgument.get(ctx, "lobby");
							PlayerRole role = PlayerRoleArgument.get(ctx, "role");
							return joinAsRole(ctx, lobby, role);
						})
					))
				);
		// @formatter:on
	}

	private static int joinAsRole(CommandContext<CommandSource> ctx, @Nullable IGameLobby givenLobby, @Nullable PlayerRole forcedRole) throws CommandSyntaxException {
		CommandSource source = ctx.getSource();
		ServerPlayerEntity player = source.asPlayer();

		GameResult<IGameLobby> lobbyResult = resolveLobby(source, givenLobby, forcedRole);
		if (lobbyResult.isError()) {
			source.sendErrorMessage(lobbyResult.getError());
			return Command.SINGLE_SUCCESS;
		}

		IGameLobby lobby = lobbyResult.getOk();
		IGameLobbyPlayers players = lobby.getPlayers();
		CompletableFuture<GameResult<Unit>> joinFuture = GameResult.handleException("An unexpected error has occurred", players.join(player));
		joinFuture.thenAcceptAsync(result -> {
			if (result.isOk()) {
				if (forcedRole != null) {
					players.forceRole(player, forcedRole);
				}
				source.sendFeedback(GameTexts.Commands.joinedLobby(lobby), false);
			} else {
				source.sendErrorMessage(result.getError());
			}
		}, source.getServer());

		return Command.SINGLE_SUCCESS;
	}

	private static GameResult<IGameLobby> resolveLobby(CommandSource source, @Nullable IGameLobby givenLobby, @Nullable PlayerRole forcedRole) {
		if (givenLobby != null) {
			return GameResult.ok(givenLobby);
		} else {
			List<? extends IGameLobby> lobbies = IGameManager.get().getVisibleLobbies(source).collect(Collectors.toList());
			if (lobbies.size() == 1) {
				return GameResult.ok(lobbies.get(0));
			} else if (lobbies.isEmpty()) {
				return GameResult.error(GameTexts.Commands.noJoinableLobbies());
			}

			return GameResult.error(GameTexts.Commands.lobbySelector(lobbies, forcedRole));
		}
	}
}
