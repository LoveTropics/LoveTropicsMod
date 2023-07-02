package com.lovetropics.minigames.client.lobby.select_role;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SelectRolePromptMessage(int lobbyId) {
	public void encode(FriendlyByteBuf buffer) {
		buffer.writeVarInt(this.lobbyId);
	}

	public static SelectRolePromptMessage decode(FriendlyByteBuf buffer) {
		return new SelectRolePromptMessage(buffer.readVarInt());
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ClientRoleSelection.openScreen(lobbyId);
	}
}
