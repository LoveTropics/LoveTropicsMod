package com.lovetropics.minigames.client.minigame;

import com.lovetropics.minigames.common.core.game.PlayerRole;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientRoleMessage {

	private final int instanceId;
	private final PlayerRole role;

	public ClientRoleMessage(int instanceId, PlayerRole role) {
		this.instanceId = instanceId;
		this.role = role;
	}

	public void encode(PacketBuffer buffer) {
		buffer.writeVarInt(instanceId);
		buffer.writeBoolean(role != null);
		if (role != null) {
			buffer.writeEnumValue(role);
		}
	}

	public static ClientRoleMessage decode(PacketBuffer buffer) {
		int instanceId = buffer.readVarInt();
		PlayerRole role = null;
		if (buffer.readBoolean()) {
			role = buffer.readEnumValue(PlayerRole.class);
		}
		return new ClientRoleMessage(instanceId, role);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ClientMinigameState.get(instanceId).ifPresent(s -> s.setRole(role));
		});
		ctx.get().setPacketHandled(true);
	}
}
