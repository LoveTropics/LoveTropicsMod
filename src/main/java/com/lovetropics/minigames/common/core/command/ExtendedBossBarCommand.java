package com.lovetropics.minigames.common.core.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.server.commands.BossBarCommands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.bossevents.CustomBossEvent;

import java.util.Collection;

import static net.minecraft.command.Commands.argument;
import staticnet.minecraft.commands.Commandss.literal;

public final class ExtendedBossBarCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		// @formatter:off
		dispatcher.register(
			literal("bossbar")
				.then(literal("players").requires(source -> source.hasPermission(2))
					.then(literal("add")
					.then(argument("id", ResourceLocationArgument.id()).suggests(BossBarCommands.SUGGEST_BOSS_BAR)
					.then(argument("players", EntityArgument.players())
						.executes(ExtendedBossBarCommand::addPlayers)
					)))
					.then(literal("remove")
					.then(argument("id", ResourceLocationArgument.id()).suggests(BossBarCommands.SUGGEST_BOSS_BAR)
					.then(argument("players", EntityArgument.players())
						.executes(ExtendedBossBarCommand::removePlayers)
					)))
			)
		);
		// @formatter:on
	}

	private static int addPlayers(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		return updatePlayers(context, CustomBossEvent::addPlayer);
	}

	private static int removePlayers(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		return updatePlayers(context, CustomBossEvent::removePlayer);
	}

	private static int updatePlayers(CommandContext<CommandSourceStack> context, PlayerUpdateFunction update) throws CommandSyntaxException {
		CustomBossEvent bossBar = BossBarCommands.getBossBar(context);
		Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");

		for (ServerPlayer player : players) {
			update.apply(bossBar, player);
		}

		return Command.SINGLE_SUCCESS;
	}

	interface PlayerUpdateFunction {
		void apply(CustomBossEvent bossBar, ServerPlayer player);
	}
}
