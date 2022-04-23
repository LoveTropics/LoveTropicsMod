package com.lovetropics.minigames.common.core.command.game;

import com.lovetropics.minigames.common.core.game.GameResult;
import com.lovetropics.minigames.common.core.game.IGameManager;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import com.lovetropics.minigames.common.core.game.util.GameTexts;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;

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
			.executes(c -> GameCommand.executeGameAction(() -> {
				CommandSource source = c.getSource();
				IGameLobby lobby = IGameManager.get().getLobbyFor(source);
				if (lobby != null && lobby.getPlayers().remove(source.getPlayerOrException())) {
					return GameResult.ok(GameTexts.Commands.leftLobby(lobby));
				}
				return GameResult.error(GameTexts.Commands.notInLobby());
			}, c.getSource()));
	}
}
