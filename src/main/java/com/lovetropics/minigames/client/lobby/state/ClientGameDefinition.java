package com.lovetropics.minigames.client.lobby.state;

import com.lovetropics.minigames.common.core.game.IGameDefinition;
import com.lovetropics.minigames.common.core.game.config.GameConfigs;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.List;
import java.util.stream.Collectors;

public final class ClientGameDefinition {
	public final ResourceLocation id;
	public final ITextComponent name;
	public final int minimumParticipants;
	public final int maximumParticipants;

	public ClientGameDefinition(ResourceLocation id, ITextComponent name, int minimumParticipants, int maximumParticipants) {
		this.id = id;
		this.name = name;
		this.minimumParticipants = minimumParticipants;
		this.maximumParticipants = maximumParticipants;
	}

	public static List<ClientGameDefinition> collectInstalled() {
		return GameConfigs.REGISTRY.stream()
				.map(ClientGameDefinition::from)
				.collect(Collectors.toList());
	}

	public static ClientGameDefinition from(IGameDefinition definition) {
		return new ClientGameDefinition(
				definition.getId(),
				definition.getName(),
				definition.getMinimumParticipantCount(),
				definition.getMaximumParticipantCount()
		);
	}

	public static ClientGameDefinition decode(PacketBuffer buffer) {
		ResourceLocation id = buffer.readResourceLocation();
		ITextComponent name = buffer.readTextComponent();
		int minimumParticipants = buffer.readVarInt();
		int maximumParticipants = buffer.readVarInt();
		return new ClientGameDefinition(id, name, minimumParticipants, maximumParticipants);
	}

	public void encode(PacketBuffer buffer) {
		buffer.writeResourceLocation(this.id);
		buffer.writeTextComponent(this.name);
		buffer.writeVarInt(this.minimumParticipants);
		buffer.writeVarInt(this.maximumParticipants);
	}
}
