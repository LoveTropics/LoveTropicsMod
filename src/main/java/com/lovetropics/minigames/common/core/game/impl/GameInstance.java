package com.lovetropics.minigames.common.core.game.impl;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.client.minigame.ClientMinigameMessage;
import com.lovetropics.minigames.common.core.game.*;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLifecycleEvents;
import com.lovetropics.minigames.common.core.game.statistics.PlayerKey;
import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.network.PacketDistributor;

public final class GameInstance implements IGameInstance {
	private final SingleGameManager manager;
	private final MinecraftServer server;
	private final GameInstanceId instanceId;
	private final IGameDefinition definition;
	private final PlayerKey initiator;

	private IGamePhase phase;

	private GameInstance(SingleGameManager manager, MinecraftServer server, GameInstanceId instanceId, IGameDefinition definition, PlayerKey initiator) {
		this.manager = manager;
		this.server = server;
		this.instanceId = instanceId;
		this.definition = definition;
		this.initiator = initiator;
	}

	static GameResult<PollingGame> createPolling(SingleGameManager manager, MinecraftServer server, IGameDefinition definition, PlayerKey initiator) {
		GameInstanceId instanceId = GameInstanceId.generate(definition);

		GameInstance instance = new GameInstance(manager, server, instanceId, definition, initiator);
		return PollingGame.create(instance).map(polling -> {
			instance.setPhase(polling);
			return polling;
		});
	}

	@Override
	public MinecraftServer getServer() {
		return server;
	}

	@Override
	public GameInstanceId getInstanceId() {
		return instanceId;
	}

	@Override
	public IGameDefinition getDefinition() {
		return definition;
	}

	@Override
	public PlayerKey getInitiator() {
		return initiator;
	}

	@Override
	public IGamePhase getPhase() {
		return phase;
	}

	boolean tick() {
		IActiveGame active = asActive();
		if (active != null) {
			try {
				active.invoker(GameLifecycleEvents.TICK).tick(active);
			} catch (Exception e) {
				LoveTropics.LOGGER.warn("Failed to dispatch world tick event", e);
				return false;
			}
		}
		return true;
	}

	void setPhase(IGamePhase phase) {
		this.phase = phase;
		LoveTropicsNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new ClientMinigameMessage(this));
	}

	void stop() {
		phase = new InactiveGame(this);
		LoveTropicsNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new ClientMinigameMessage(instanceId.networkId));

		manager.stop(this);
	}
}
