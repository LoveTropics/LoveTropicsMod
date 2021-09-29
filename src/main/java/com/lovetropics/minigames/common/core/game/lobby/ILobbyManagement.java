package com.lovetropics.minigames.common.core.game.lobby;

import com.lovetropics.minigames.common.core.game.IGameDefinition;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;

public interface ILobbyManagement {
	boolean startManaging(ServerPlayerEntity player);

	void stopManaging(ServerPlayerEntity player);

	boolean canManage(CommandSource source);

	void setName(String name);

	void enqueueGame(IGameDefinition game);

	void removeQueuedGame(int id);

	void selectControl(LobbyControls.Type type);

	void setVisibility(LobbyVisibility visibility);

	void close();
}
