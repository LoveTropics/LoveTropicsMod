package com.lovetropics.minigames.common.core.network;

import com.lovetropics.minigames.client.ClientPlayerDisguises;
import com.lovetropics.minigames.common.core.diguise.DisguiseType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Supplier;

public record PlayerDisguiseMessage(UUID player, @Nullable DisguiseType disguise) {
	public void encode(FriendlyByteBuf buffer) {
		buffer.writeUUID(player);
		buffer.writeNullable(disguise, (b, d) -> d.encode(b));
	}

	public static PlayerDisguiseMessage decode(FriendlyByteBuf buffer) {
		UUID player = buffer.readUUID();
		DisguiseType disguise = buffer.readNullable(DisguiseType::decode);
		return new PlayerDisguiseMessage(player, disguise);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ClientPlayerDisguises.updateClientDisguise(player, disguise);
	}
}
