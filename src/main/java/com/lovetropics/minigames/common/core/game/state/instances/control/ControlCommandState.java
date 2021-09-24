package com.lovetropics.minigames.common.core.game.state.instances.control;

import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import com.lovetropics.minigames.common.core.game.state.GameStateType;
import com.lovetropics.minigames.common.core.game.state.IGameState;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.command.CommandSource;

import java.util.Map;
import java.util.stream.Stream;

public final class ControlCommandState implements IGameState {
	public static final GameStateType.Defaulted<ControlCommandState> TYPE = GameStateType.create("Control Commands", ControlCommandState::new);

	private final Map<String, ControlCommand> commands = new Object2ObjectOpenHashMap<>();

	public void add(String name, ControlCommand command) {
		this.commands.put(name, command);
	}

	public void invoke(IGameLobby lobby, String name, CommandSource source) throws CommandSyntaxException {
		ControlCommand command = this.commands.get(name);
		if (command != null) {
			command.invoke(source, lobby.getMetadata().initiator());
		}
	}

	public Stream<String> list(IGameLobby lobby, CommandSource source) {
		return this.commands.entrySet().stream()
				.filter(entry -> entry.getValue().canUse(source, lobby.getMetadata().initiator()))
				.map(Map.Entry::getKey);
	}
}
