package com.lovetropics.minigames.client.minigame;

import com.lovetropics.minigames.common.core.game.PlayerRole;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PlayerCountsMessage {
	private final int instanceId;
	private final PlayerRole role;
	private final int count;

	public PlayerCountsMessage(int instanceId, PlayerRole role, int count) {
		this.instanceId = instanceId;
		this.role = role;
		this.count = count;
	}

	public void encode(PacketBuffer buffer) {
		buffer.writeVarInt(instanceId);
		buffer.writeEnumValue(role);
		buffer.writeInt(count);
	}

	public static PlayerCountsMessage decode(PacketBuffer buffer) {
		int instanceId = buffer.readVarInt();
		PlayerRole role = buffer.readEnumValue(PlayerRole.class);
		int count = buffer.readInt();
		return new PlayerCountsMessage(instanceId, role, count);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ClientMinigameState.get(instanceId).ifPresent(s -> s.setMemberCount(role, count));
		});
		ctx.get().setPacketHandled(true);
	}
}
