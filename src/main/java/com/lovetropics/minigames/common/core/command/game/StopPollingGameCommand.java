package com.lovetropics.minigames.common.core.command.game;

import com.lovetropics.minigames.client.data.TropicraftLangKeys;
import com.lovetropics.minigames.common.core.game.IGameManager;
import com.lovetropics.minigames.common.core.game.polling.PollingGameInstance;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.util.text.TranslationTextComponent;

import static net.minecraft.command.Commands.literal;

public class StopPollingGameCommand {
	public static void register(final CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			literal("game")
			.then(literal("stop_poll").requires(s -> s.hasPermissionLevel(2))
			.executes(c -> GameCommand.executeMinigameAction(() -> {
				PollingGameInstance game = IGameManager.get().getPollingGame();
				if (game == null) {
					throw new SimpleCommandExceptionType(new TranslationTextComponent(TropicraftLangKeys.COMMAND_NO_MINIGAME_POLLING)).create();
				}
				return IGameManager.get().stopPolling(game);
			}, c.getSource())))
		);
	}
}
