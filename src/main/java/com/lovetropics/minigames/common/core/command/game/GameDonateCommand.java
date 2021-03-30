package com.lovetropics.minigames.common.core.command.game;

import com.lovetropics.minigames.common.core.game.GameManager;
import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePackageEvents;
import com.lovetropics.minigames.common.core.integration.game_actions.GamePackage;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;

import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.Commands.literal;

public class GameDonateCommand {
	public static void register(final CommandDispatcher<CommandSource> dispatcher) {
		// @formatter:off
		dispatcher.register(
			literal("game")
				.then(literal("donate")
				.requires(s -> s.hasPermissionLevel(4))
				.then(argument("type", StringArgumentType.string())
				.executes(c -> execute(c.getSource(), StringArgumentType.getString(c, "type"))
			)))
		);
		// @formatter:on
	}

	private static int execute(CommandSource source, String packageType) throws CommandSyntaxException {
		final IGameInstance instance = GameManager.get().getActiveMinigame();
		if (instance != null) {
			GamePackage gamePackage = new GamePackage(packageType, source.getName(), source.asPlayer().getUniqueID());
			instance.events().invoker(GamePackageEvents.RECEIVE_PACKAGE).onReceivePackage(instance, gamePackage);
			return Command.SINGLE_SUCCESS;
		}
		return 0;
	}
}
