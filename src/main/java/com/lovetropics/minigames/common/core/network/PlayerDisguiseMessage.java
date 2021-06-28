package com.lovetropics.minigames.common.core.network;

import com.lovetropics.minigames.common.core.diguise.PlayerDisguise;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Supplier;

public class PlayerDisguiseMessage {
	private final UUID player;
	private final EntityType<?> disguise;

	public PlayerDisguiseMessage(UUID player, @Nullable EntityType<?> disguise) {
		this.player = player;
		this.disguise = disguise;
	}

	public void encode(PacketBuffer buffer) {
		buffer.writeUniqueId(player);

		buffer.writeBoolean(disguise != null);
		if (disguise != null) {
			buffer.writeVarInt(Registry.ENTITY_TYPE.getId(disguise));
		}
	}

	public static PlayerDisguiseMessage decode(PacketBuffer buffer) {
		UUID player = buffer.readUniqueId();

		EntityType<?> disguise;
		if (buffer.readBoolean()) {
			disguise = Registry.ENTITY_TYPE.getByValue(buffer.readVarInt());
		} else {
			disguise = null;
		}

		return new PlayerDisguiseMessage(player, disguise);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ClientWorld world = Minecraft.getInstance().world;

			PlayerEntity player = world.getPlayerByUuid(this.player);
			if (player != null) {
				PlayerDisguise.get(player).ifPresent(playerDisguise -> {
					if (disguise != null) {
						Entity entity = disguise.create(world);
						playerDisguise.setDisguiseEntity(entity);
					} else {
						playerDisguise.setDisguiseEntity(null);
					}
				});
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
