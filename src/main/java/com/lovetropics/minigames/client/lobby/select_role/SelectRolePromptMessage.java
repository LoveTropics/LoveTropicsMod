package com.lovetropics.minigames.client.lobby.select_role;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public final class SelectRolePromptMessage {
	private final int lobbyId;

	public SelectRolePromptMessage(int lobbyId) {
		this.lobbyId = lobbyId;
	}

	public void encode(PacketBuffer buffer) {
		buffer.writeVarInt(this.lobbyId);
	}

	public static SelectRolePromptMessage decode(PacketBuffer buffer) {
		return new SelectRolePromptMessage(buffer.readVarInt());
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ClientRoleSelection.openScreen(lobbyId);
		});
		ctx.get().setPacketHandled(true);
	}
}
