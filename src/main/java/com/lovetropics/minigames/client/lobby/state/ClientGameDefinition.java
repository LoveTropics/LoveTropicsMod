package com.lovetropics.minigames.client.lobby.state;

import com.lovetropics.minigames.common.core.game.IGameDefinition;
import com.lovetropics.minigames.common.core.game.config.GameConfigs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

public final class ClientGameDefinition {
	public final ResourceLocation id;
	public final Component name;
	@Nullable
	public final Component subtitle;
	@Nullable
	public final ResourceLocation icon;
	public final int minimumParticipants;
	public final int maximumParticipants;

	public ClientGameDefinition(
			ResourceLocation id,
			Component name, @Nullable Component subtitle,
			@Nullable ResourceLocation icon,
			int minimumParticipants, int maximumParticipants
	) {
		this.id = id;
		this.name = name;
		this.subtitle = subtitle;
		this.icon = icon;
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
				definition.getSubtitle(),
				definition.getIcon(),
				definition.getMinimumParticipantCount(),
				definition.getMaximumParticipantCount()
		);
	}

	public static ClientGameDefinition decode(FriendlyByteBuf buffer) {
		ResourceLocation id = buffer.readResourceLocation();
		Component name = buffer.readComponent();
		Component subtitle = buffer.readBoolean() ? buffer.readComponent() : null;
		ResourceLocation icon = buffer.readBoolean() ? buffer.readResourceLocation() : null;
		int minimumParticipants = buffer.readVarInt();
		int maximumParticipants = buffer.readVarInt();
		return new ClientGameDefinition(id, name, subtitle, icon, minimumParticipants, maximumParticipants);
	}

	public void encode(FriendlyByteBuf buffer) {
		buffer.writeResourceLocation(this.id);
		buffer.writeComponent(this.name);
		buffer.writeBoolean(this.subtitle != null);
		if (this.subtitle != null) {
			buffer.writeComponent(this.subtitle);
		}
		buffer.writeBoolean(this.icon != null);
		if (this.icon != null) {
			buffer.writeResourceLocation(this.icon);
		}
		buffer.writeVarInt(this.minimumParticipants);
		buffer.writeVarInt(this.maximumParticipants);
	}
}
