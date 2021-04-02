package com.lovetropics.minigames.common.core.command.game;

import com.lovetropics.minigames.client.data.LoveTropicsLangKeys;
import com.lovetropics.minigames.common.core.game.GameResult;
import com.lovetropics.minigames.common.core.game.IGameManager;
import com.lovetropics.minigames.common.core.game.PlayerRole;
import com.lovetropics.minigames.common.core.game.ProtoGameInstance;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import java.util.Collection;

import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.Commands.literal;

public class JoinGameCommand {
	private static final DynamicCommandExceptionType ROLE_NOT_VALID = new DynamicCommandExceptionType(key -> {
		return new StringTextComponent("'" + key + "' is not a valid role");
	});

	public static void register(final CommandDispatcher<CommandSource> dispatcher) {
		// @formatter:off
		dispatcher.register(
			literal("game")
				.then(registerBuilder("register"))
				.then(registerBuilder("join"))
				.then(registerBuilder("play"))
				.then(literal("spectate").executes(ctx -> registerAsRole(ctx, PlayerRole.SPECTATOR)))
		);
		// @formatter:on
	}

	private static LiteralArgumentBuilder<CommandSource> registerBuilder(String name) {
		// @formatter:off
		return literal(name)
				.executes(ctx -> registerAsRole(ctx, null))
				.then(argument("role", StringArgumentType.word())
						.requires(source -> source.hasPermissionLevel(2))
						.suggests((context, builder) -> ISuggestionProvider.suggest(
								PlayerRole.stream().map(PlayerRole::getKey),
								builder
						))
						.executes(JoinGameCommand::registerAsRole)
				);
		// @formatter:on
	}

	private static int registerAsRole(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
		String roleKey = StringArgumentType.getString(ctx, "role");
		PlayerRole role = PlayerRole.byKey(roleKey);
		if (role == null) {
			throw ROLE_NOT_VALID.create(roleKey);
		}

		return registerAsRole(ctx, role);
	}

	private static int registerAsRole(CommandContext<CommandSource> ctx, @Nullable PlayerRole requestedRole) throws CommandSyntaxException {
		return GameCommand.executeMinigameAction(() -> {
			ServerPlayerEntity player = ctx.getSource().asPlayer();

			// TODO: support concurrent minigames
			Collection<ProtoGameInstance> games = IGameManager.get().getAllGames();
			if (games.size() == 1) {
				if (games.iterator().next().requestPlayerJoin(player, requestedRole)) {
					return GameResult.ok(new TranslationTextComponent(LoveTropicsLangKeys.COMMAND_REGISTERED_FOR_MINIGAME));
				} else {
					return GameResult.error(new TranslationTextComponent(LoveTropicsLangKeys.COMMAND_MINIGAME_ALREADY_REGISTERED));
				}
			} else {
				return GameResult.error(new TranslationTextComponent(LoveTropicsLangKeys.COMMAND_NO_MINIGAME_POLLING));
			}
		}, ctx.getSource());
	}
}
