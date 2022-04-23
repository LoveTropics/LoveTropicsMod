package com.lovetropics.minigames.common.core.command.game;

import com.lovetropics.minigames.common.core.game.GameResult;
import com.lovetropics.minigames.common.core.game.GameStopReason;
import com.lovetropics.minigames.common.core.game.IGameManager;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.util.GameTexts;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;

import static net.minecraft.commands.Commands.literal;

public class FinishGameCommand {
	public static void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(
				literal("game")
						.then(literal("finish")
								.executes(c -> GameCommand.executeGameAction(() -> {
									IGamePhase game = IGameManager.get().getGamePhaseFor(c.getSource());
									if (game == null) {
										return GameResult.error(GameTexts.Commands.notInGame());
									}
									return game.requestStop(GameStopReason.finished()).map($ -> {
										return GameTexts.Commands.stoppedGame(game.getDefinition());
									});
								}, c.getSource())))
		);
	}
}
