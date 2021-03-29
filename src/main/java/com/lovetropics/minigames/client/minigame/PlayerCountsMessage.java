package com.lovetropics.minigames.client.minigame;

import java.util.function.Supplier;

import com.lovetropics.minigames.common.core.game.PlayerRole;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PlayerCountsMessage {

	private final PlayerRole role;
	private final int count;

	public PlayerCountsMessage(PlayerRole role, int count) {
		this.role = role;
		this.count = count;
	}

	public void encode(PacketBuffer buffer) {
		buffer.writeEnumValue(role);
		buffer.writeInt(count);
	}

	public static PlayerCountsMessage decode(PacketBuffer buffer) {
		PlayerRole role = buffer.readEnumValue(PlayerRole.class);
		int count = buffer.readInt();
		return new PlayerCountsMessage(role, count);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ClientMinigameState.get().ifPresent(s -> s.setMemberCount(role, count));
		});
		ctx.get().setPacketHandled(true);
	}
}
