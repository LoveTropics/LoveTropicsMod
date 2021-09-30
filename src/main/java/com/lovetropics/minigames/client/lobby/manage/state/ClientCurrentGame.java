package com.lovetropics.minigames.client.lobby.manage.state;

import com.lovetropics.minigames.client.lobby.state.ClientGameDefinition;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;

public final class ClientCurrentGame {
	private final ClientGameDefinition definition;
	@Nullable
	private final ITextComponent error;

	public ClientCurrentGame(ClientGameDefinition definition, @Nullable ITextComponent error) {
		this.definition = definition;
		this.error = error;
	}

	public ClientGameDefinition definition() {
		return definition;
	}

	@Nullable
	public ITextComponent error() {
		return error;
	}

	public void encode(PacketBuffer buffer) {
		definition.encode(buffer);

		buffer.writeBoolean(error != null);
		if (error != null) {
			buffer.writeTextComponent(error);
		}
	}

	public static ClientCurrentGame decode(PacketBuffer buffer) {
		ClientGameDefinition definition = ClientGameDefinition.decode(buffer);
		ITextComponent error = buffer.readBoolean() ? buffer.readTextComponent() : null;
		return new ClientCurrentGame(definition, error);
	}
}
