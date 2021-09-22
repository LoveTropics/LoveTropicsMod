package com.lovetropics.minigames.client.lobby;

import com.lovetropics.minigames.client.lobby.screen.ManageLobbyScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public final class ManageLobbyMessage {
	private final int lobbyId;

	public ManageLobbyMessage(int lobbyId) {
		this.lobbyId = lobbyId;
	}

	public void encode(PacketBuffer buffer) {
		buffer.writeVarInt(this.lobbyId);
	}

	public static ManageLobbyMessage decode(PacketBuffer buffer) {
		return new ManageLobbyMessage(buffer.readVarInt());
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			openScreen();
		});
		ctx.get().setPacketHandled(true);
	}

	// TODO: dedicated server
	private static void openScreen() {
		Minecraft.getInstance().displayGuiScreen(new ManageLobbyScreen());
	}
}
