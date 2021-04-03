package com.lovetropics.minigames.common.core.command.game;

import com.lovetropics.minigames.client.data.LoveTropicsLangKeys;
import com.lovetropics.minigames.common.core.game.GameMessages;
import com.lovetropics.minigames.common.core.game.GameResult;
import com.lovetropics.minigames.common.core.game.IActiveGame;
import com.lovetropics.minigames.common.core.game.IGameManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;

import static net.minecraft.command.Commands.literal;

public class LeaveGameCommand {
	public static void register(final CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			literal("game")
			.then(unregisterBuilder("unregister"))
			.then(unregisterBuilder("leave"))
		);
	}

	private static LiteralArgumentBuilder<CommandSource> unregisterBuilder(String name) {
		return literal(name).requires(s -> s.getEntity() instanceof ServerPlayerEntity)
			.executes(c -> GameCommand.executeMinigameAction(() -> {
				CommandSource source = c.getSource();
				IActiveGame game = IGameManager.get().getActiveGameFor(source);
				if (game != null && game.removePlayer(source.asPlayer())) {
					return GameResult.ok(GameMessages.forGame(game).unregisterSuccess());
				}
				return GameResult.error(new TranslationTextComponent(LoveTropicsLangKeys.COMMAND_NOT_REGISTERED_FOR_MINIGAME));
			}, c.getSource()));
	}
}
