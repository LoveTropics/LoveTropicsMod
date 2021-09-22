package com.lovetropics.minigames.common.core.command.game;

import com.lovetropics.minigames.client.data.LoveTropicsLangKeys;
import com.lovetropics.minigames.common.core.game.GameStopReason;
import com.lovetropics.minigames.common.core.game.IActiveGame;
import com.lovetropics.minigames.common.core.game.IGameManager;
import com.lovetropics.minigames.common.core.game.util.GameMessages;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.util.text.TranslationTextComponent;

import static net.minecraft.command.Commands.literal;

public class CancelGameCommand {
	public static void register(final CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			literal("game")
			.then(literal("cancel").requires(s -> s.hasPermissionLevel(2))
			.executes(c -> GameCommand.executeGameAction(() -> {
				IActiveGame game = IGameManager.get().getGameFor(c.getSource());
				if (game == null) {
					throw new SimpleCommandExceptionType(new TranslationTextComponent(LoveTropicsLangKeys.COMMAND_NO_MINIGAME)).create();
				}
				return game.stop(GameStopReason.CANCELED).map(u -> GameMessages.forLobby(game.getLobby()).stopSuccess());
			}, c.getSource())))
		);
	}
}
