package com.lovetropics.minigames.common.command.minigames;

import com.lovetropics.minigames.common.minigames.IMinigameDefinition;
import com.lovetropics.minigames.common.minigames.MinigameManager;
import com.lovetropics.minigames.common.minigames.statistics.PlayerKey;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;

import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.Commands.literal;

public class CommandPollMinigame {
	public static void register(final CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			literal("minigame")
			.then(literal("poll").requires(s -> s.hasPermissionLevel(2))
			.then(argument("minigame_id", ResourceLocationArgument.resourceLocation())
		              .suggests((ctx, sb) -> ISuggestionProvider.suggest(
		                      MinigameManager.getInstance().getAllMinigames().stream()
		                          .map(IMinigameDefinition::getID)
		                          .map(ResourceLocation::toString), sb))
		              .requires(s -> s.hasPermissionLevel(2))
			.executes(c -> {
				ResourceLocation id = ResourceLocationArgument.getResourceLocation(c, "minigame_id");
				ServerPlayerEntity player = c.getSource().asPlayer();

				int result = CommandMinigame.executeMinigameAction(() -> MinigameManager.getInstance().startPolling(id, PlayerKey.from(player)), c.getSource());
				if (result == 1) {
					CommandMinigame.executeMinigameAction(() -> MinigameManager.getInstance().joinPlayerAs(player, null), c.getSource());
				}

				return result;
		}))));
	}
}
