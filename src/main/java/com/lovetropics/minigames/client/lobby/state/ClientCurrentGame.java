package com.lovetropics.minigames.client.lobby.state;

import com.lovetropics.minigames.common.core.game.GamePhaseType;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.client_tweak.GameClientTweakMap;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;

public final class ClientCurrentGame {
	private final ClientGameDefinition definition;
	private final GamePhaseType phase;
	// TODO: do we need to detach tweaks from here so that we can send them and have them be live-updated
	private final GameClientTweakMap tweaks;

	@Nullable
	private final ITextComponent error;

	private ClientCurrentGame(ClientGameDefinition definition, GamePhaseType phase, GameClientTweakMap tweaks, @Nullable ITextComponent error) {
		this.definition = definition;
		this.phase = phase;
		this.error = error;
		this.tweaks = tweaks;
	}

	public static ClientCurrentGame create(ClientGameDefinition definition, GamePhaseType phaseType, GameClientTweakMap tweaks) {
		return new ClientCurrentGame(definition, phaseType, tweaks, null);
	}

	public static ClientCurrentGame create(IGamePhase phase) {
		ClientGameDefinition definition = ClientGameDefinition.from(phase.getDefinition());
		GamePhaseType phaseType = phase.getPhaseType();
		return ClientCurrentGame.create(definition, phaseType, phase.getClientTweaks());
	}

	public ClientCurrentGame withError(ITextComponent error) {
		return new ClientCurrentGame(definition, phase, tweaks, error);
	}

	public ClientGameDefinition definition() {
		return definition;
	}

	public GamePhaseType phase() {
		return phase;
	}

	public GameClientTweakMap tweaks() {
		return tweaks;
	}

	@Nullable
	public ITextComponent error() {
		return error;
	}

	public void encode(PacketBuffer buffer) {
		definition.encode(buffer);
		buffer.writeEnumValue(phase);

		tweaks.encode(buffer);

		buffer.writeBoolean(error != null);
		if (error != null) {
			buffer.writeTextComponent(error);
		}
	}

	public static ClientCurrentGame decode(PacketBuffer buffer) {
		ClientGameDefinition definition = ClientGameDefinition.decode(buffer);
		GamePhaseType phase = buffer.readEnumValue(GamePhaseType.class);
		GameClientTweakMap tweaks = GameClientTweakMap.decode(buffer);
		ITextComponent error = buffer.readBoolean() ? buffer.readTextComponent() : null;
		return new ClientCurrentGame(definition, phase, tweaks, error);
	}
}
