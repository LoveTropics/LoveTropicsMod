package com.lovetropics.minigames.client.lobby.state;

import com.lovetropics.minigames.common.core.game.GamePhaseType;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;

public final class ClientCurrentGame {
	private final ClientGameDefinition definition;
	private final GamePhaseType phase;
	@Nullable
	private final ITextComponent error;

	private ClientCurrentGame(ClientGameDefinition definition, GamePhaseType phase, @Nullable ITextComponent error) {
		this.definition = definition;
		this.phase = phase;
		this.error = error;
	}

	public static ClientCurrentGame create(ClientGameDefinition definition, GamePhaseType phaseType) {
		return new ClientCurrentGame(definition, phaseType, null);
	}

	public static ClientCurrentGame create(IGamePhase phase) {
		ClientGameDefinition definition = ClientGameDefinition.from(phase.getDefinition());
		GamePhaseType phaseType = phase.getPhaseType();
		return ClientCurrentGame.create(definition, phaseType);
	}

	public ClientCurrentGame withError(ITextComponent error) {
		return new ClientCurrentGame(definition, phase, error);
	}

	public ClientGameDefinition definition() {
		return definition;
	}

	public GamePhaseType phase() {
		return phase;
	}

	@Nullable
	public ITextComponent error() {
		return error;
	}

	public void encode(PacketBuffer buffer) {
		definition.encode(buffer);
		buffer.writeEnum(phase);

		buffer.writeBoolean(error != null);
		if (error != null) {
			buffer.writeComponent(error);
		}
	}

	public static ClientCurrentGame decode(PacketBuffer buffer) {
		ClientGameDefinition definition = ClientGameDefinition.decode(buffer);
		GamePhaseType phase = buffer.readEnum(GamePhaseType.class);
		ITextComponent error = buffer.readBoolean() ? buffer.readComponent() : null;
		return new ClientCurrentGame(definition, phase, error);
	}
}
