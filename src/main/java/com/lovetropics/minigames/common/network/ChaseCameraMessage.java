package com.lovetropics.minigames.common.network;

import com.lovetropics.minigames.client.chase.ChaseCameraManager;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class ChaseCameraMessage {
	private final List<UUID> players;

	public ChaseCameraMessage(List<UUID> players) {
		this.players = players;
	}

	public void encode(PacketBuffer buffer) {
		buffer.writeVarInt(players.size());
		for (UUID player : players) {
			buffer.writeUniqueId(player);
		}
	}

	public static ChaseCameraMessage decode(PacketBuffer buffer) {
		int count = buffer.readVarInt();
		List<UUID> players = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			players.add(buffer.readUniqueId());
		}

		return new ChaseCameraMessage(players);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ChaseCameraManager.update(players);
		});
		ctx.get().setPacketHandled(true);
	}
}
