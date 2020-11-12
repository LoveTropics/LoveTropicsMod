package com.lovetropics.minigames.common.command.minigames;

import com.lovetropics.minigames.common.game_actions.DonationPackageGameAction;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.MinigameManager;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;

import java.util.UUID;

import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.Commands.literal;

public class CommandMinigameDonate {
	public static void register(final CommandDispatcher<CommandSource> dispatcher) {
		// @formatter:off
		dispatcher.register(
			literal("minigame")
				.then(literal("donate")
				.requires(s -> s.hasPermissionLevel(4))
				.then(argument("type", StringArgumentType.string())
				.executes(c -> execute(c.getSource(), StringArgumentType.getString(c, "type"))
			)))
		);
		// @formatter:on
	}

	private static int execute(CommandSource source, String packageType) throws CommandSyntaxException {
		final IMinigameInstance instance = MinigameManager.getInstance().getActiveMinigame();
		if (instance != null) {
			DonationPackageGameAction action = new DonationPackageGameAction(UUID.randomUUID(), packageType, "0", source.getName(), source.asPlayer().getUniqueID());
			for (IMinigameBehavior behavior : instance.getBehaviors()) {
				behavior.onDonationPackageRequested(instance, action);
			}
			return Command.SINGLE_SUCCESS;
		}
		return 0;
	}
}
