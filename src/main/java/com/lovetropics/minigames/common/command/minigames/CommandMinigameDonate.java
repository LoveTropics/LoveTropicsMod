package com.lovetropics.minigames.common.command.minigames;

import com.lovetropics.minigames.common.game_actions.DonationPackageGameAction;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.MinigameManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;

import java.util.UUID;

import static net.minecraft.command.Commands.literal;

public class CommandMinigameDonate {
	public static void register(final CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			literal("minigame")
			.then(literal("donate").requires(s -> s.hasPermissionLevel(4))
			.executes(c -> execute(c.getSource())))
		);
	}

	private static int execute(CommandSource source) throws CommandSyntaxException {
		DonationPackageGameAction act = new DonationPackageGameAction(UUID.randomUUID(),
				"lightning_storm", "0", "Corosus", UUID.fromString("380df991-f603-344c-a090-369bad2a924a"));
		final IMinigameInstance instance = MinigameManager.getInstance().getActiveMinigame();
		if (instance != null) {
			instance.getBehaviors().stream().anyMatch(behavior -> act.notifyBehavior(instance, behavior));
			return 1;
		}
		return 0;
	}
}
