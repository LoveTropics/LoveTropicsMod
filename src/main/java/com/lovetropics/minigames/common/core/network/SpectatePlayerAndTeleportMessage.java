package com.lovetropics.minigames.common.core.network;

import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class SpectatePlayerAndTeleportMessage {
	private final UUID player;

	public SpectatePlayerAndTeleportMessage(UUID player) {
		this.player = player;
	}

	public void encode(FriendlyByteBuf buffer) {
		buffer.writeUUID(player);
	}

	public static SpectatePlayerAndTeleportMessage decode(FriendlyByteBuf buffer) {
		UUID player = buffer.readUUID();
		return new SpectatePlayerAndTeleportMessage(player);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayer sender = ctx.get().getSender();
			if (sender == null || !sender.isSpectator()) {
				return;
			}

			Player target = sender.level.getPlayerByUUID(player);
			if (target != null) {
				sender.teleportTo(sender.getLevel(), target.getX(), target.getY(), target.getZ(), target.yRot, target.xRot);
			}

			sender.setCamera(target);
		});
		ctx.get().setPacketHandled(true);
	}
}
