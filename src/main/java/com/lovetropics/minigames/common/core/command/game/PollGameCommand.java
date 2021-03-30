package com.lovetropics.minigames.common.core.command.game;

import com.lovetropics.minigames.common.core.game.IGameDefinition;
import com.lovetropics.minigames.common.core.game.GameManager;
import com.lovetropics.minigames.common.core.game.statistics.PlayerKey;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;

import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.Commands.literal;

public class PollGameCommand {
	public static void register(final CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			literal("game")
			.then(literal("poll").requires(s -> s.hasPermissionLevel(2))
			.then(argument("minigame_id", ResourceLocationArgument.resourceLocation())
		              .suggests((ctx, sb) -> ISuggestionProvider.suggest(
		                      GameManager.get().getAllMinigames().stream()
		                          .map(IGameDefinition::getID)
		                          .map(ResourceLocation::toString), sb))
		              .requires(s -> s.hasPermissionLevel(2))
			.executes(c -> {
				ResourceLocation id = ResourceLocationArgument.getResourceLocation(c, "minigame_id");
				ServerPlayerEntity player = c.getSource().asPlayer();

				int result = GameCommand.executeMinigameAction(() -> GameManager.get().startPolling(id, PlayerKey.from(player)), c.getSource());
				if (result == 1) {
					GameCommand.executeMinigameAction(() -> GameManager.get().joinPlayerAs(player, null), c.getSource());
				}

				return result;
		}))));
	}
}
