package com.lovetropics.minigames.common.command.minigames;

import com.lovetropics.minigames.common.minigames.MinigameManager;
import com.lovetropics.minigames.common.minigames.PlayerRole;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;

import javax.annotation.Nullable;

import static net.minecraft.command.Commands.literal;

public class CommandRegisterMinigame {
	public static void register(final CommandDispatcher<CommandSource> dispatcher) {
		// @formatter:off
		dispatcher.register(
			literal("minigame")
				.then(literal("register")
					.executes(ctx -> registerAsRole(ctx, null))
					.then(literal("participant")
						.requires(source -> source.hasPermissionLevel(2))
						.executes(ctx -> registerAsRole(ctx, PlayerRole.PARTICIPANT))
					)
					.then(literal("spectator")
						.requires(source -> source.hasPermissionLevel(2))
						.executes(ctx -> registerAsRole(ctx, PlayerRole.SPECTATOR))
					)
				)
				.then(literal("spectate").executes(ctx -> registerAsRole(ctx, PlayerRole.SPECTATOR)))
		);
		// @formatter:on
	}

	private static int registerAsRole(CommandContext<CommandSource> ctx, @Nullable PlayerRole requestedRole) throws CommandSyntaxException {
		return CommandMinigame.executeMinigameAction(() -> {
			ServerPlayerEntity player = ctx.getSource().asPlayer();
			return MinigameManager.getInstance().registerFor(player, requestedRole);
		}, ctx.getSource());
	}
}
