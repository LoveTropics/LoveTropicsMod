package com.lovetropics.minigames.common.core.command.game;

import com.lovetropics.minigames.client.data.LoveTropicsLangKeys;
import com.lovetropics.minigames.common.core.command.argument.GameConfigArgument;
import com.lovetropics.minigames.common.core.game.IGameManager;
import com.lovetropics.minigames.common.core.game.config.GameConfig;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;

import static net.minecraft.command.Commands.literal;

public class PollGameCommand {
	public static void register(final CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			literal("game")
			.then(literal("poll").requires(s -> s.hasPermissionLevel(2))
			.then(GameConfigArgument.argument("game_id").requires(s -> s.hasPermissionLevel(2))
			.executes(c -> {
				IGameManager gameManager = IGameManager.get();
				GameConfig gameConfig = GameConfigArgument.get(c, "game_id");
				ServerPlayerEntity player = c.getSource().asPlayer();

				return GameCommand.executeMinigameAction(() -> {
					return gameManager.startPolling(gameConfig, player).map(polling -> {
						polling.requestPlayerJoin(player, null);
						return new TranslationTextComponent(LoveTropicsLangKeys.COMMAND_MINIGAME_POLLED);
					});
				}, c.getSource());
		}))));
	}
}
