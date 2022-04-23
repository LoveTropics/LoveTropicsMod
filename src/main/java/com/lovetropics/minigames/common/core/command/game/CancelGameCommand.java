package com.lovetropics.minigames.common.core.command.game;

import com.lovetropics.minigames.common.core.game.GameResult;
import com.lovetropics.minigames.common.core.game.GameStopReason;
import com.lovetropics.minigames.common.core.game.IGameManager;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.util.GameTexts;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import static net.minecraft.command.Commands.literal;

public class CancelGameCommand {
	public static void register(final CommandDispatcher<CommandSource> dispatcher) {
		// @formatter:off
		dispatcher.register(
			literal("game")
				.then(literal("cancel").requires(s -> s.hasPermission(2))
					.executes(context -> cancel(context, false))
					.then(literal("confirm")
						.executes(context -> cancel(context, true))
					)
				)
		);
		// @formatter:on
	}

	private static int cancel(CommandContext<CommandSource> ctx, boolean confirmed) throws CommandSyntaxException {
		return GameCommand.executeGameAction(() -> {
			IGamePhase game = IGameManager.get().getGamePhaseFor(ctx.getSource());
			if (game == null) {
				return GameResult.error(GameTexts.Commands.notInGame());
			}

			if (!confirmed && shouldRequireConfirmation(ctx.getSource(), game)) {
				ITextComponent message = new StringTextComponent("Please confirm that you would like to cancel this game! ")
						.append(GameTexts.clickHere("/game cancel confirm"));
				return GameResult.error(message);
			}

			return game.requestStop(GameStopReason.canceled()).map(u -> GameTexts.Commands.stoppedGame(game.getDefinition()));
		}, ctx.getSource());
	}

	private static boolean shouldRequireConfirmation(CommandSource source, IGamePhase game) {
		return source.getEntity() instanceof PlayerEntity && game.getAllPlayers().size() > 1;
	}
}
