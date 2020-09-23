package com.lovetropics.minigames.common.network;

import com.lovetropics.minigames.Constants;
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
	}
}