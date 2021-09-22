package com.lovetropics.minigames.common.core.game.state.instances.control;

import com.lovetropics.minigames.common.core.game.IGamePhase;
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

	static ControlCommandInvoker forGame(IGamePhase game) {
		return new ControlCommandInvoker() {
			@Override
			public void invoke(String name, CommandSource source) throws CommandSyntaxException {
				game.getState().get(ControlCommandState.TYPE).invoke(game.getLobby(), name, source);
			}

			@Override
			public Stream<String> list(CommandSource source) {
				return game.getState().get(ControlCommandState.TYPE).list(game.getLobby(), source);
			}
		};
	}

	void invoke(String name, CommandSource source) throws CommandSyntaxException;

	Stream<String> list(CommandSource source);
}
