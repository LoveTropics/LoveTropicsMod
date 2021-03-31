package com.lovetropics.minigames.common.core.game.control;

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

	void invoke(String name, CommandSource source) throws CommandSyntaxException;

	Stream<String> list(CommandSource source);
}
