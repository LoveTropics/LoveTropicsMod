package com.lovetropics.minigames.client.minigame;

import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PlayerCountsMessage {
	private final int lobbyId;
	private final PlayerRole role;
	private final int count;

	public PlayerCountsMessage(int lobbyId, PlayerRole role, int count) {
		this.lobbyId = lobbyId;
		this.role = role;
		this.count = count;
	}

	public void encode(PacketBuffer buffer) {
		buffer.writeVarInt(lobbyId);
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
			ClientMinigameState.get(lobbyId).ifPresent(s -> s.setMemberCount(role, count));
		});
		ctx.get().setPacketHandled(true);
	}
}
