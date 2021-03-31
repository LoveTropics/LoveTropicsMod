package com.lovetropics.minigames.common.core.command.game;

import com.lovetropics.minigames.common.core.game.IGameDefinition;
import com.lovetropics.minigames.common.core.game.IGameManager;
import com.lovetropics.minigames.common.core.game.config.GameConfig;
import com.lovetropics.minigames.common.core.game.config.GameConfigs;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.Commands.literal;

public class PollGameCommand {
	public static void register(final CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			literal("game")
			.then(literal("poll").requires(s -> s.hasPermissionLevel(2))
			.then(argument("minigame_id", ResourceLocationArgument.resourceLocation())
		              .suggests((ctx, sb) -> ISuggestionProvider.suggest(
		                      GameConfigs.GAME_CONFIGS.values().stream()
		                          .map(IGameDefinition::getId)
		                          .map(ResourceLocation::toString), sb))
		              .requires(s -> s.hasPermissionLevel(2))
			.executes(c -> {
				ResourceLocation id = ResourceLocationArgument.getResourceLocation(c, "minigame_id");
				ServerPlayerEntity player = c.getSource().asPlayer();

				GameConfig gameConfig = GameConfigs.GAME_CONFIGS.get(id);
				if (gameConfig == null) {
					throw new SimpleCommandExceptionType(new StringTextComponent("Minigame with id " + id + " does not exist!")).create();
				}

				int result = GameCommand.executeMinigameAction(() -> IGameManager.get().startPolling(gameConfig, player), c.getSource());
				if (result == 1) {
					GameCommand.executeMinigameAction(() -> IGameManager.get().joinPlayerAs(player, null), c.getSource());
				}

				return result;
		}))));
	}
}
