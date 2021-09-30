package com.lovetropics.minigames.common.core.command.game;

import com.lovetropics.minigames.common.core.command.argument.GameLobbyArgument;
import com.lovetropics.minigames.common.core.command.argument.PlayerRoleArgument;
import com.lovetropics.minigames.common.core.game.GameResult;
import com.lovetropics.minigames.common.core.game.IGameManager;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.util.GameTexts;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;

import javax.annotation.Nullable;
import java.util.List;
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
				.then(literal("spectate")
					.executes(ctx -> joinAsRole(ctx, null, PlayerRole.SPECTATOR))
					.then(GameLobbyArgument.argument("lobby")
						.executes(ctx -> {
							IGameLobby lobby = GameLobbyArgument.get(ctx, "lobby");
							return joinAsRole(ctx, lobby, PlayerRole.SPECTATOR);
						})
					)
				)
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

	private static int joinAsRole(CommandContext<CommandSource> ctx, @Nullable IGameLobby givenLobby, @Nullable PlayerRole requestedRole) throws CommandSyntaxException {
		CommandSource source = ctx.getSource();
		ServerPlayerEntity player = source.asPlayer();
		return GameCommand.executeGameAction(() -> {
			return resolveLobby(source, givenLobby, requestedRole).andThen(lobby -> {
				if (lobby.getPlayers().join(player, requestedRole)) {
					return GameResult.ok(GameTexts.Commands.joinedLobby(lobby));
				} else {
					return GameResult.error(GameTexts.Commands.alreadyInLobby());
				}
			});
		}, source);
	}

	private static GameResult<IGameLobby> resolveLobby(CommandSource source, @Nullable IGameLobby givenLobby, @Nullable PlayerRole requestedRole) {
		if (givenLobby != null) {
			return GameResult.ok(givenLobby);
		} else {
			List<? extends IGameLobby> publicLobbies = IGameManager.get().getVisibleLobbies(source).collect(Collectors.toList());
			if (publicLobbies.size() == 1) {
				return GameResult.ok(publicLobbies.get(0));
			} else if (publicLobbies.isEmpty()) {
				return GameResult.error(GameTexts.Commands.noJoinableLobbies());
			}

			return GameResult.error(GameTexts.Commands.lobbySelector(publicLobbies, requestedRole));
		}
	}
}
