package com.lovetropics.minigames.common.core.game.state.control;

import com.lovetropics.minigames.common.core.game.lobby.GameLobbyMetadata;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;

import java.util.stream.Stream;

public interface ControlCommandInvoker {
	ControlCommandInvoker EMPTY = new ControlCommandInvoker() {
		@Override
		public void invoke(String name, CommandSourceStack source) {
		}

		@Override
		public Stream<String> list(CommandSourceStack source) {
			return Stream.empty();
		}
	};

	static ControlCommandInvoker create(ControlCommands commands, GameLobbyMetadata lobby) {
		return new ControlCommandInvoker() {
			@Override
			public void invoke(String name, CommandSourceStack source) throws CommandSyntaxException {
				commands.invoke(lobby, name, source);
			}

			@Override
			public Stream<String> list(CommandSourceStack source) {
				return commands.list(lobby, source);
			}
		};
	}

	void invoke(String name, CommandSourceStack source) throws CommandSyntaxException;

	Stream<String> list(CommandSourceStack source);
}
