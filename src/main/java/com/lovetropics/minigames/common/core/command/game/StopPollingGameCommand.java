package com.lovetropics.minigames.common.core.command.game;

import com.lovetropics.minigames.client.data.LoveTropicsLangKeys;
import com.lovetropics.minigames.common.core.game.GameMessages;
import com.lovetropics.minigames.common.core.game.IGameManager;
import com.lovetropics.minigames.common.core.game.IPollingGame;
import com.lovetropics.minigames.common.core.game.impl.PollingGame;
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
				IPollingGame game = IGameManager.get().getPollingGameFor(c.getSource());
				if (game == null) {
					throw new SimpleCommandExceptionType(new TranslationTextComponent(LoveTropicsLangKeys.COMMAND_NO_MINIGAME_POLLING)).create();
				}

				PollingGame polling = (PollingGame) game;
				return polling.cancel().map(u -> GameMessages.forGame(game).stopPollSuccess());
			}, c.getSource())))
		);
	}
}
