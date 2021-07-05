package com.lovetropics.minigames.common.core.network;

import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.lovetropics.minigames.client.ClientPlayerDisguises;

import net.minecraft.entity.EntityType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.fml.network.NetworkEvent;

public class PlayerDisguiseMessage {
	private final UUID player;
	private final EntityType<?> disguise;

	public PlayerDisguiseMessage(UUID player, @Nullable EntityType<?> disguise) {
		this.player = player;
		this.disguise = disguise;
	}

	public void encode(PacketBuffer buffer) {
		buffer.writeUniqueId(player);

		buffer.writeBoolean(disguise != null);
		if (disguise != null) {
			buffer.writeVarInt(Registry.ENTITY_TYPE.getId(disguise));
		}
	}

	public static PlayerDisguiseMessage decode(PacketBuffer buffer) {
		UUID player = buffer.readUniqueId();

		EntityType<?> disguise;
		if (buffer.readBoolean()) {
			disguise = Registry.ENTITY_TYPE.getByValue(buffer.readVarInt());
		} else {
			disguise = null;
		}

		return new PlayerDisguiseMessage(player, disguise);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> ClientPlayerDisguises.updateClientDisguise(player, disguise));
		ctx.get().setPacketHandled(true);
	}
}
