package com.lovetropics.minigames.client.minigame;

import com.lovetropics.minigames.common.core.game.GameStatus;
import com.lovetropics.minigames.common.core.game.IProtoGame;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientMinigameMessage {

	private final int instanceId;
	private final ResourceLocation minigame;
	private final String unlocName;
	private final GameStatus status;
	private final int maxPlayers;

	public ClientMinigameMessage(int instanceId) {
		this(instanceId, null, null, null, 0);
	}

	public ClientMinigameMessage(IProtoGame game) {
		this(game.getInstanceId().networkId, game.getDefinition().getDisplayId(),
			game.getDefinition().getTranslationKey(),
			game.getStatus(),
			game.getDefinition().getMaximumParticipantCount());
	}

	private ClientMinigameMessage(int instanceId, ResourceLocation minigame, String unlocName, GameStatus status, int maxPlayers) {
		this.instanceId = instanceId;
		this.minigame = minigame;
		this.unlocName = unlocName;
		this.status = status;
		this.maxPlayers = maxPlayers;
	}

	public void encode(PacketBuffer buffer) {
		buffer.writeVarInt(instanceId);
		buffer.writeBoolean(minigame != null);
		if (minigame != null) {
			buffer.writeResourceLocation(minigame);
			buffer.writeString(unlocName, 200);
			buffer.writeEnumValue(status);
			buffer.writeInt(maxPlayers);
		}
	}

	public static ClientMinigameMessage decode(PacketBuffer buffer) {
		int instanceId = buffer.readVarInt();
		if (buffer.readBoolean()) {
			ResourceLocation minigame = buffer.readResourceLocation();
			String unlocName = buffer.readString(200);
			GameStatus status = buffer.readEnumValue(GameStatus.class);
			int maxPlayers = buffer.readInt();
			return new ClientMinigameMessage(instanceId, minigame, unlocName, status, maxPlayers);
		}
		return new ClientMinigameMessage(instanceId);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ClientMinigameState state = minigame == null ? null : new ClientMinigameState(instanceId, minigame, unlocName, status, maxPlayers);
			ClientMinigameState.update(instanceId, state);
		});
		ctx.get().setPacketHandled(true);
	}
}
