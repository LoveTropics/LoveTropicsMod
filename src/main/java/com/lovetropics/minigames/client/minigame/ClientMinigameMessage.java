package com.lovetropics.minigames.client.minigame;

import java.util.function.Supplier;

import com.lovetropics.minigames.common.minigames.MinigameStatus;
import com.lovetropics.minigames.common.minigames.ProtoMinigame;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClientMinigameMessage {

	private final ResourceLocation minigame;
	private final String unlocName;
	private final MinigameStatus status;
	private final int maxPlayers;

	public ClientMinigameMessage() {
		this(null, null, null, 0);
	}

	public ClientMinigameMessage(ProtoMinigame minigame) {
		this(minigame.getDefinition().getID(),
			minigame.getDefinition().getUnlocalizedName(),
			minigame.getStatus(),
			minigame.getDefinition().getMaximumParticipantCount());
	}

	private ClientMinigameMessage(ResourceLocation minigame, String unlocName, MinigameStatus status, int maxPlayers) {
		this.minigame = minigame;
		this.unlocName = unlocName;
		this.status = status;
		this.maxPlayers = maxPlayers;
	}

	public void encode(PacketBuffer buffer) {
		buffer.writeBoolean(minigame != null);
		if (minigame != null) {
			buffer.writeResourceLocation(minigame);
			buffer.writeString(unlocName, 200);
			buffer.writeEnumValue(status);
			buffer.writeInt(maxPlayers);
		}
	}

	public static ClientMinigameMessage decode(PacketBuffer buffer) {
		if (buffer.readBoolean()) {
			ResourceLocation minigame = buffer.readResourceLocation();
			String unlocName = buffer.readString(200);
			MinigameStatus status = buffer.readEnumValue(MinigameStatus.class);
			int maxPlayers = buffer.readInt();
			return new ClientMinigameMessage(minigame, unlocName, status, maxPlayers);
		}
		return new ClientMinigameMessage();
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ClientMinigameState.update(minigame == null ? null : new ClientMinigameState(minigame, unlocName, status, maxPlayers));
		});
		ctx.get().setPacketHandled(true);
	}
}
