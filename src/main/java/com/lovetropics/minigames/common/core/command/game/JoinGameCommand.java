package com.lovetropics.minigames.common.core.command.game;

import com.lovetropics.minigames.client.data.LoveTropicsLangKeys;
import com.lovetropics.minigames.common.core.command.argument.GameInstanceArgument;
import com.lovetropics.minigames.common.core.command.argument.PlayerRoleArgument;
import com.lovetropics.minigames.common.core.game.*;
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
					.then(GameInstanceArgument.argument("game")
						.executes(ctx -> {
							IGameInstance game = GameInstanceArgument.get(ctx, "game");
							return registerAsRole(ctx, game, PlayerRole.SPECTATOR);
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
				.then(GameInstanceArgument.argument("game")
					.executes(ctx -> {
						IGameInstance game = GameInstanceArgument.get(ctx, "game");
						return registerAsRole(ctx, game, null);
					})
					.then(literal("as").requires(source -> source.hasPermissionLevel(2))
						.then(PlayerRoleArgument.argument("role")
						.executes(ctx -> {
							IGameInstance game = GameInstanceArgument.get(ctx, "game");
							PlayerRole role = PlayerRoleArgument.get(ctx, "role");
							return registerAsRole(ctx, game, role);
						})
					))
				);
		// @formatter:on
	}

	private static int registerAsRole(CommandContext<CommandSource> ctx, @Nullable IGameInstance givenGame, @Nullable PlayerRole requestedRole) throws CommandSyntaxException {
		return GameCommand.executeMinigameAction(() -> {
			ServerPlayerEntity player = ctx.getSource().asPlayer();
			return resolveGame(givenGame, requestedRole).flatMap(game -> {
				if (game.requestPlayerJoin(player, requestedRole)) {
					return GameResult.ok(GameMessages.forGame(game).registerSuccess());
				} else {
					return GameResult.error(new TranslationTextComponent(LoveTropicsLangKeys.COMMAND_MINIGAME_ALREADY_REGISTERED));
				}
			});
		}, ctx.getSource());
	}

	private static GameResult<IGameInstance> resolveGame(@Nullable IGameInstance givenGame, @Nullable PlayerRole requestedRole) {
		if (givenGame != null) {
			return GameResult.ok(givenGame);
		} else {
			Collection<? extends IGameInstance> games = IGameManager.get().getAllGames();
			if (games.size() == 1) {
				return GameResult.ok(games.iterator().next());
			} else if (games.isEmpty()){
				return GameResult.error(new TranslationTextComponent(LoveTropicsLangKeys.COMMAND_NO_MINIGAME_POLLING));
			}

			return GameResult.error(buildGameSelection(requestedRole, games));
		}
	}

	private static IFormattableTextComponent buildGameSelection(@Nullable PlayerRole requestedRole, Collection<? extends IGameInstance> games) {
		IFormattableTextComponent selection = new StringTextComponent("There are multiple games available to join! Select one from this list:\n")
				.mergeStyle(TextFormatting.GOLD, TextFormatting.BOLD);

		for (IGameInstance game : games) {
			IGameDefinition definition = game.getDefinition();

			String joinCommand = getJoinCommand(game.getInstanceId(), requestedRole);

			ITextComponent joinLink = new StringTextComponent("Click to join")
					.modifyStyle(style -> {
						return style.setFormatting(TextFormatting.BLUE)
								.setUnderlined(true)
								.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, joinCommand))
								.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent(joinCommand)));
					});

			IFormattableTextComponent line = new StringTextComponent(" - ").mergeStyle(TextFormatting.GRAY)
					.appendSibling(definition.getName().deepCopy().mergeStyle(TextFormatting.AQUA))
					.appendSibling(new StringTextComponent(" (" + game.getAllPlayers().size() + " players)").mergeStyle(TextFormatting.GREEN))
					.appendString(": ")
					.appendSibling(joinLink)
					.appendString("\n");

			selection = selection.appendSibling(line);
		}

		return selection;
	}

	private static String getJoinCommand(GameInstanceId game, @Nullable PlayerRole requestedRole) {
		String command = "/game join " + game.commandId;
		if (requestedRole != null) {
			command += " as " + requestedRole.getKey();
		}
		return command;
	}
}
