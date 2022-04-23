package com.lovetropics.minigames.common.core.network;

import com.lovetropics.minigames.client.ClientPlayerDisguises;
import com.lovetropics.minigames.common.core.diguise.DisguiseType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Supplier;

public class PlayerDisguiseMessage {
	private final UUID player;
	private final DisguiseType disguise;

	public PlayerDisguiseMessage(UUID player, @Nullable DisguiseType disguise) {
		this.player = player;
		this.disguise = disguise;
	}

	public void encode(FriendlyByteBuf buffer) {
		buffer.writeUUID(player);

		buffer.writeBoolean(disguise != null);
		if (disguise != null) {
			disguise.encode(buffer);
		}
	}

	public static PlayerDisguiseMessage decode(FriendlyByteBuf buffer) {
		UUID player = buffer.readUUID();

		DisguiseType disguise;
		if (buffer.readBoolean()) {
			disguise = DisguiseType.decode(buffer);
		} else {
			disguise = null;
		}

		return new PlayerDisguiseMessage(player, disguise);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> ClientPlayerDisguises.updateClientDisguise(player, disguise));
		ctx.get().setPacketHandled(true);
	}
}
