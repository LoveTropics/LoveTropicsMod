package com.lovetropics.minigames.common.core.command.game;

import com.lovetropics.minigames.client.data.LoveTropicsLangKeys;
import com.lovetropics.minigames.common.core.command.argument.GameLobbyArgument;
import com.lovetropics.minigames.common.core.command.argument.PlayerRoleArgument;
import com.lovetropics.minigames.common.core.game.*;
import com.lovetropics.minigames.common.core.game.lobby.GameLobbyMetadata;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.util.GameMessages;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import javax.annotation.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static net.minecraft.command.Commands.literal;

public class JoinGameCommand {
	public static void register(final CommandDispatcher<CommandSource> dispatcher) {
		// @formatter:off
		dispatcher.register(
			literal("game")
				.then(registerBuilder("register"))
				.then(registerBuilder("join"))
				.then(registerBuilder("play"))
				.then(literal("spectate")
					.executes(ctx -> registerAsRole(ctx, null, PlayerRole.SPECTATOR))
					.then(GameLobbyArgument.argument("lobby")
						.executes(ctx -> {
							IGameLobby lobby = GameLobbyArgument.get(ctx, "lobby");
							return registerAsRole(ctx, lobby, PlayerRole.SPECTATOR);
						})
					)
				)
		);
		// @formatter:on
	}

	private static LiteralArgumentBuilder<CommandSource> registerBuilder(String name) {
		// @formatter:off
		return literal(name)
				.executes(ctx -> registerAsRole(ctx, null, null))
				.then(GameLobbyArgument.argument("lobby")
					.executes(ctx -> {
						IGameLobby lobby = GameLobbyArgument.get(ctx, "lobby");
						return registerAsRole(ctx, lobby, null);
					})
					.then(literal("as").requires(source -> source.hasPermissionLevel(2))
						.then(PlayerRoleArgument.argument("role")
						.executes(ctx -> {
							IGameLobby lobby = GameLobbyArgument.get(ctx, "lobby");
							PlayerRole role = PlayerRoleArgument.get(ctx, "role");
							return registerAsRole(ctx, lobby, role);
						})
					))
				);
		// @formatter:on
	}

	private static int registerAsRole(CommandContext<CommandSource> ctx, @Nullable IGameLobby givenLobby, @Nullable PlayerRole requestedRole) throws CommandSyntaxException {
		CommandSource source = ctx.getSource();
		ServerPlayerEntity player = source.asPlayer();
		return GameCommand.executeGameAction(() -> {
			return resolveLobby(source, givenLobby, requestedRole).andThen(lobby -> {
				if (lobby.getPlayers().register(player, requestedRole)) {
					return GameResult.ok(GameMessages.forLobby(lobby).registerSuccess());
				} else {
					return GameResult.error(new TranslationTextComponent(LoveTropicsLangKeys.COMMAND_MINIGAME_ALREADY_REGISTERED));
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
			} else if (publicLobbies.isEmpty()){
				return GameResult.error(new TranslationTextComponent(LoveTropicsLangKeys.COMMAND_NO_MINIGAME_POLLING));
			}

			return GameResult.error(buildLobbySelector(requestedRole, publicLobbies));
		}
	}

	private static IFormattableTextComponent buildLobbySelector(@Nullable PlayerRole requestedRole, Collection<? extends IGameLobby> lobbies) {
		IFormattableTextComponent selector = new StringTextComponent("There are multiple games lobbies available to join! Select one from this list:\n")
				.mergeStyle(TextFormatting.GOLD, TextFormatting.BOLD);

		for (IGameLobby lobby : lobbies) {
			String joinCommand = getJoinCommand(lobby.getMetadata(), requestedRole);

			ITextComponent joinLink = new StringTextComponent("Click to join")
					.modifyStyle(style -> {
						return style.setFormatting(TextFormatting.BLUE)
								.setUnderlined(true)
								.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, joinCommand))
								.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent(joinCommand)));
					});

			IFormattableTextComponent line = new StringTextComponent(" - ").mergeStyle(TextFormatting.GRAY)
					.appendSibling(new StringTextComponent(lobby.getMetadata().name()).mergeStyle(TextFormatting.AQUA))
					.appendSibling(new StringTextComponent(" (" + lobby.getPlayers().size() + " players)").mergeStyle(TextFormatting.GREEN))
					.appendString(": ")
					.appendSibling(joinLink)
					.appendString("\n");

			selector = selector.appendSibling(line);
		}

		return selector;
	}

	private static String getJoinCommand(GameLobbyMetadata lobby, @Nullable PlayerRole requestedRole) {
		String command = "/game join " + lobby.commandId();
		if (requestedRole != null) {
			command += " as " + requestedRole.getKey();
		}
		return command;
	}
}
