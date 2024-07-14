package com.lovetropics.minigames.client.lobby.state;

import com.lovetropics.minigames.common.core.game.IGameDefinition;
import com.lovetropics.minigames.common.core.game.config.GameConfigs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

public final class ClientGameDefinition {
	public static final StreamCodec<RegistryFriendlyByteBuf, ClientGameDefinition> STREAM_CODEC = StreamCodec.of((output, definition) -> definition.encode(output), ClientGameDefinition::decode);

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

	public static ClientGameDefinition decode(RegistryFriendlyByteBuf buffer) {
		ResourceLocation id = buffer.readResourceLocation();
		Component name = ComponentSerialization.STREAM_CODEC.decode(buffer);
		Component subtitle = buffer.readBoolean() ? ComponentSerialization.STREAM_CODEC.decode(buffer) : null;
		ResourceLocation icon = buffer.readBoolean() ? buffer.readResourceLocation() : null;
		int minimumParticipants = buffer.readVarInt();
		int maximumParticipants = buffer.readVarInt();
		return new ClientGameDefinition(id, name, subtitle, icon, minimumParticipants, maximumParticipants);
	}

	public void encode(RegistryFriendlyByteBuf buffer) {
		buffer.writeResourceLocation(id);
		ComponentSerialization.STREAM_CODEC.encode(buffer, name);
		buffer.writeBoolean(subtitle != null);
		if (subtitle != null) {
			ComponentSerialization.STREAM_CODEC.encode(buffer, subtitle);
		}
		buffer.writeBoolean(icon != null);
		if (icon != null) {
			buffer.writeResourceLocation(icon);
		}
		buffer.writeVarInt(minimumParticipants);
		buffer.writeVarInt(maximumParticipants);
	}
}
