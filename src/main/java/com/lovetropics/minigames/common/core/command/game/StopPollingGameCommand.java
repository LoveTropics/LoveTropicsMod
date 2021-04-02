package com.lovetropics.minigames.common.core.command.game;

import com.lovetropics.minigames.client.data.LoveTropicsLangKeys;
import com.lovetropics.minigames.common.core.game.GameMessages;
import com.lovetropics.minigames.common.core.game.IGameManager;
import com.lovetropics.minigames.common.core.game.ProtoGameInstance;
import com.lovetropics.minigames.common.core.game.polling.PollingGameInstance;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Collection;

import static net.minecraft.command.Commands.literal;

public class StopPollingGameCommand {
	public static void register(final CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			literal("game")
			.then(literal("stop_poll").requires(s -> s.hasPermissionLevel(2))
			.executes(c -> GameCommand.executeMinigameAction(() -> {
				// TODO: this is terrible
				Collection<ProtoGameInstance> games = IGameManager.get().getAllGames();
				if (games.size() != 1) {
					throw new SimpleCommandExceptionType(new TranslationTextComponent(LoveTropicsLangKeys.COMMAND_NO_MINIGAME_POLLING)).create();
				}

				ProtoGameInstance game = games.iterator().next();
				if (!(game instanceof PollingGameInstance)) {
					throw new SimpleCommandExceptionType(new TranslationTextComponent(LoveTropicsLangKeys.COMMAND_NO_MINIGAME_POLLING)).create();
				}

				PollingGameInstance polling = (PollingGameInstance) game;
				return IGameManager.get().stopPolling(polling).map(u -> GameMessages.forGame(game).stopPolling());
			}, c.getSource())))
		);
	}
}
