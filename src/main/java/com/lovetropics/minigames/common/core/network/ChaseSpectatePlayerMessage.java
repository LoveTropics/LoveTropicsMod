package com.lovetropics.minigames.common.core.network;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class ChaseSpectatePlayerMessage {
	private final UUID player;

	public ChaseSpectatePlayerMessage(UUID player) {
		this.player = player;
	}

	public void encode(PacketBuffer buffer) {
		buffer.writeUniqueId(player);
	}

	public static ChaseSpectatePlayerMessage decode(PacketBuffer buffer) {
		UUID player = buffer.readUniqueId();
		return new ChaseSpectatePlayerMessage(player);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayerEntity sender = ctx.get().getSender();
			if (sender == null || !sender.isSpectator()) {
				return;
			}

			PlayerEntity target = sender.world.getPlayerByUuid(player);
			if (target != null) {
				sender.teleport(sender.getServerWorld(), target.getPosX(), target.getPosY(), target.getPosZ(), target.rotationYaw, target.rotationPitch);
			}

			sender.setSpectatingEntity(target);
		});
		ctx.get().setPacketHandled(true);
	}
}
