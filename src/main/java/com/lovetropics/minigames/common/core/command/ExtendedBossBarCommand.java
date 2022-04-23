package com.lovetropics.minigames.common.core.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.command.impl.BossBarCommand;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.CustomServerBossInfo;

import java.util.Collection;

import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.Commands.literal;

public final class ExtendedBossBarCommand {
	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		// @formatter:off
		dispatcher.register(
			literal("bossbar")
				.then(literal("players").requires(source -> source.hasPermission(2))
					.then(literal("add")
					.then(argument("id", ResourceLocationArgument.id()).suggests(BossBarCommand.SUGGEST_BOSS_BAR)
					.then(argument("players", EntityArgument.players())
						.executes(ExtendedBossBarCommand::addPlayers)
					)))
					.then(literal("remove")
					.then(argument("id", ResourceLocationArgument.id()).suggests(BossBarCommand.SUGGEST_BOSS_BAR)
					.then(argument("players", EntityArgument.players())
						.executes(ExtendedBossBarCommand::removePlayers)
					)))
			)
		);
		// @formatter:on
	}

	private static int addPlayers(CommandContext<CommandSource> context) throws CommandSyntaxException {
		return updatePlayers(context, CustomServerBossInfo::addPlayer);
	}

	private static int removePlayers(CommandContext<CommandSource> context) throws CommandSyntaxException {
		return updatePlayers(context, CustomServerBossInfo::removePlayer);
	}

	private static int updatePlayers(CommandContext<CommandSource> context, PlayerUpdateFunction update) throws CommandSyntaxException {
		CustomServerBossInfo bossBar = BossBarCommand.getBossBar(context);
		Collection<ServerPlayerEntity> players = EntityArgument.getPlayers(context, "players");

		for (ServerPlayerEntity player : players) {
			update.apply(bossBar, player);
		}

		return Command.SINGLE_SUCCESS;
	}

	interface PlayerUpdateFunction {
		void apply(CustomServerBossInfo bossBar, ServerPlayerEntity player);
	}
}
