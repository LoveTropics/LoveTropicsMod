package com.lovetropics.minigames.common.core.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public record SpectatePlayerAndTeleportMessage(UUID player) {
	public void encode(FriendlyByteBuf buffer) {
		buffer.writeUUID(player);
	}

	public static SpectatePlayerAndTeleportMessage decode(FriendlyByteBuf buffer) {
		UUID player = buffer.readUUID();
		return new SpectatePlayerAndTeleportMessage(player);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ServerPlayer sender = ctx.get().getSender();
		if (sender == null || !sender.isSpectator()) {
			return;
		}

		Player target = sender.level().getPlayerByUUID(player);
		if (target != null) {
			sender.teleportTo(sender.serverLevel(), target.getX(), target.getY(), target.getZ(), target.getYRot(), target.getXRot());
		}

		sender.setCamera(target);
	}
}
