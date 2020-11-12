package com.lovetropics.minigames.common.command.minigames;

import com.lovetropics.minigames.common.minigames.MinigameManager;
import com.lovetropics.minigames.common.minigames.PlayerRole;
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

import javax.annotation.Nullable;

import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.Commands.literal;

public class CommandRegisterMinigame {
	private static final DynamicCommandExceptionType ROLE_NOT_VALID = new DynamicCommandExceptionType(key -> {
		return new StringTextComponent("'" + key + "' is not a valid role");
	});

	public static void register(final CommandDispatcher<CommandSource> dispatcher) {
		// @formatter:off
		dispatcher.register(
			literal("minigame")
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
						.executes(CommandRegisterMinigame::registerAsRole)
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
		return CommandMinigame.executeMinigameAction(() -> {
			ServerPlayerEntity player = ctx.getSource().asPlayer();
			return MinigameManager.getInstance().joinPlayerAs(player, requestedRole);
		}, ctx.getSource());
	}
}
