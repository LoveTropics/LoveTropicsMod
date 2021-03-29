package com.lovetropics.minigames.client.minigame;

import java.util.function.Supplier;

import com.lovetropics.minigames.common.core.game.PlayerRole;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClientRoleMessage {

	private final PlayerRole role;

	public ClientRoleMessage(PlayerRole role) {
		this.role = role;
	}

	public void encode(PacketBuffer buffer) {
		buffer.writeBoolean(role != null);
		if (role != null) {
			buffer.writeEnumValue(role);
		}
	}

	public static ClientRoleMessage decode(PacketBuffer buffer) {
		PlayerRole role = null;
		if (buffer.readBoolean()) {
			role = buffer.readEnumValue(PlayerRole.class);
		}
		return new ClientRoleMessage(role);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ClientMinigameState.get().ifPresent(s -> s.setRole(role));
		});
		ctx.get().setPacketHandled(true);
	}
}
