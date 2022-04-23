package com.lovetropics.minigames.common.core.network;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class SpectatePlayerAndTeleportMessage {
	private final UUID player;

	public SpectatePlayerAndTeleportMessage(UUID player) {
		this.player = player;
	}

	public void encode(PacketBuffer buffer) {
		buffer.writeUUID(player);
	}

	public static SpectatePlayerAndTeleportMessage decode(PacketBuffer buffer) {
		UUID player = buffer.readUUID();
		return new SpectatePlayerAndTeleportMessage(player);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayerEntity sender = ctx.get().getSender();
			if (sender == null || !sender.isSpectator()) {
				return;
			}

			PlayerEntity target = sender.level.getPlayerByUUID(player);
			if (target != null) {
				sender.teleportTo(sender.getLevel(), target.getX(), target.getY(), target.getZ(), target.yRot, target.xRot);
			}

			sender.setCamera(target);
		});
		ctx.get().setPacketHandled(true);
	}
}
