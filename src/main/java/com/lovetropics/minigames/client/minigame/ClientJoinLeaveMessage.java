package com.lovetropics.minigames.client.minigame;

import java.util.function.Supplier;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClientJoinLeaveMessage {

	private final boolean joined;

	public ClientJoinLeaveMessage(boolean joined) {
		this.joined = joined;
	}

	public void encode(PacketBuffer buffer) {
		buffer.writeBoolean(joined);
	}

	public static ClientJoinLeaveMessage decode(PacketBuffer buffer) {
		return new ClientJoinLeaveMessage(buffer.readBoolean());
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ClientMinigameState.get().ifPresent(s -> s.setJoined(joined));
		});
		ctx.get().setPacketHandled(true);
	}
}
