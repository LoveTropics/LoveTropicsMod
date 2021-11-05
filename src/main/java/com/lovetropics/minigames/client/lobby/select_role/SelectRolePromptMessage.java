package com.lovetropics.minigames.client.lobby.select_role;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public final class SelectRolePromptMessage {
	public SelectRolePromptMessage() {
	}

	public void encode(PacketBuffer buffer) {
	}

	public static SelectRolePromptMessage decode(PacketBuffer buffer) {
		return new SelectRolePromptMessage();
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(SelectRolePromptMessage::openScreen);
		ctx.get().setPacketHandled(true);
	}

	private static void openScreen() {
		ClientRoleSelection.openScreen();
	}
}
