package com.lovetropics.minigames.common.network;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.client.minigame.ClientRoleMessage;
import com.lovetropics.minigames.client.minigame.ClientMinigameMessage;
import com.lovetropics.minigames.common.network.map.AddWorkspaceRegionMessage;
import com.lovetropics.minigames.common.network.map.SetWorkspaceMessage;
import com.lovetropics.minigames.common.network.map.UpdateWorkspaceRegionMessage;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public final class LTNetwork {
	private static final String PROTOCOL_VERSION = "1";

	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(Constants.MODID, "main"),
			() -> PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals,
			PROTOCOL_VERSION::equals
	);

	public static void register() {
		CHANNEL.messageBuilder(SetWorkspaceMessage.class, 0)
				.encoder(SetWorkspaceMessage::encode)
				.decoder(SetWorkspaceMessage::decode)
				.consumer(SetWorkspaceMessage::handle)
				.add();

		CHANNEL.messageBuilder(AddWorkspaceRegionMessage.class, 1)
				.encoder(AddWorkspaceRegionMessage::encode)
				.decoder(AddWorkspaceRegionMessage::decode)
				.consumer(AddWorkspaceRegionMessage::handle)
				.add();

		CHANNEL.messageBuilder(UpdateWorkspaceRegionMessage.class, 2)
				.encoder(UpdateWorkspaceRegionMessage::encode)
				.decoder(UpdateWorkspaceRegionMessage::decode)
				.consumer(UpdateWorkspaceRegionMessage::handle)
				.add();

		CHANNEL.messageBuilder(ChaseCameraMessage.class, 3)
				.encoder(ChaseCameraMessage::encode)
				.decoder(ChaseCameraMessage::decode)
				.consumer(ChaseCameraMessage::handle)
				.add();

		CHANNEL.messageBuilder(StopChaseCameraMessage.class, 4)
				.encoder((p, b) -> {})
				.decoder(buffer -> new StopChaseCameraMessage())
				.consumer(StopChaseCameraMessage::handle)
				.add();

		CHANNEL.messageBuilder(ChaseSpectatePlayerMessage.class, 5)
				.encoder(ChaseSpectatePlayerMessage::encode)
				.decoder(ChaseSpectatePlayerMessage::decode)
				.consumer(ChaseSpectatePlayerMessage::handle)
				.add();

		CHANNEL.messageBuilder(ClientMinigameMessage.class, 6)
				.encoder(ClientMinigameMessage::encode)
				.decoder(ClientMinigameMessage::decode)
				.consumer(ClientMinigameMessage::handle)
				.add();

		CHANNEL.messageBuilder(ClientRoleMessage.class, 7)
				.encoder(ClientRoleMessage::encode)
				.decoder(ClientRoleMessage::decode)
				.consumer(ClientRoleMessage::handle)
				.add();
	}
}
