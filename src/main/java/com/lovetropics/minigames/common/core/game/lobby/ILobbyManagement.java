package com.lovetropics.minigames.common.core.game.lobby;

import com.lovetropics.minigames.common.core.game.IGameDefinition;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;

public interface ILobbyManagement {
	boolean startManaging(ServerPlayer player);

	void stopManaging(ServerPlayer player);

	boolean canManage(CommandSourceStack source);

	void setName(String name);

	void enqueueGame(IGameDefinition game);

	void removeQueuedGame(int id);

	void reorderQueuedGame(int id, int newIndex);

	@Nullable
	QueuedGame getQueuedGame(int id);

	void selectControl(LobbyControls.Type type);

	void setVisibility(LobbyVisibility visibility);

	void close();
}
