package com.lovetropics.minigames.common.core.game.state.control;

import com.lovetropics.minigames.common.core.game.lobby.GameLobbyMetadata;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;

import java.util.stream.Stream;

public interface ControlCommandInvoker {
	ControlCommandInvoker EMPTY = new ControlCommandInvoker() {
		@Override
		public void invoke(String name, CommandSource source) {
		}

		@Override
		public Stream<String> list(CommandSource source) {
			return Stream.empty();
		}
	};

	static ControlCommandInvoker create(ControlCommands commands, GameLobbyMetadata lobby) {
		return new ControlCommandInvoker() {
			@Override
			public void invoke(String name, CommandSource source) throws CommandSyntaxException {
				commands.invoke(lobby, name, source);
			}

			@Override
			public Stream<String> list(CommandSource source) {
				return commands.list(lobby, source);
			}
		};
	}

	void invoke(String name, CommandSource source) throws CommandSyntaxException;

	Stream<String> list(CommandSource source);
}
