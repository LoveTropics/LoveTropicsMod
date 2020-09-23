package com.lovetropics.minigames.common.network;

import com.lovetropics.minigames.common.network.map.UpdateMapWorkspaceMessage;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.tropicraft.Constants;

public final class LTNetwork {
	private static final String PROTOCOL_VERSION = "1";

	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(Constants.MODID, "map"),
			() -> PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals,
			PROTOCOL_VERSION::equals
	);

	public static void register() {
		CHANNEL.messageBuilder(UpdateMapWorkspaceMessage.class, 0)
				.encoder(UpdateMapWorkspaceMessage::encode)
				.decoder(UpdateMapWorkspaceMessage::decode)
				.consumer(UpdateMapWorkspaceMessage::handle)
				.add();
	}
}
